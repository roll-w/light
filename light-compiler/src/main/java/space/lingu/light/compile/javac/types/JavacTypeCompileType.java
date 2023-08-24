/*
 * Copyright (C) 2022 Lingu Light Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.lingu.light.compile.javac.types;

import com.squareup.javapoet.TypeName;
import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.javac.TypeUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author RollW
 */
public class JavacTypeCompileType implements TypeCompileType {
    private final TypeMirror typeMirror;
    private final TypeElement element;
    private final ProcessEnv processEnv;
    private final JavacTypeCompileType superclass;
    private final List<JavacTypeCompileType> interfaces;

    public JavacTypeCompileType(TypeMirror typeMirror,
                                TypeElement element,
                                ProcessEnv processEnv) {
        this.typeMirror = typeMirror;
        this.element = element;
        this.processEnv = processEnv;
        this.superclass = initSuperclass();
        this.interfaces = initInterfaces();
    }


    @Override
    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    @Override
    public TypeElement getElement() {
        return element;
    }

    @Override
    public String getName() {
        if (element == null) {
            return typeMirror.toString();
        }
        return getSimpleName().toString();
    }

    @Override
    public Name getSimpleName() {
        return getElement().getSimpleName();
    }

    @Override
    public String getSignature() {
        if (element == null) {
            return typeMirror.toString();
        }
        return getQualifiedName().toString();
    }

    @Override
    public Name getQualifiedName() {
        return element.getQualifiedName();
    }

    @Override
    public TypeName toTypeName() {
        if (typeMirror == null) {
            return null;
        }
        return TypeName.get(typeMirror);
    }

    @Override
    public List<MethodCompileType> getDeclaredMethods() {
        // TODO:
        if (element == null) {
            return Collections.emptyList();
        }
        return getAllMethods(element);
    }

    @Override
    public List<MethodCompileType> getMethods() {
        if (element == null) {
            return Collections.emptyList();
        }
        List<MethodCompileType> declaredMethods = new ArrayList<>(getDeclaredMethods());
        List<MethodCompileType> superMethods =
                getSuperClassesMethods(declaredMethods);
        declaredMethods.addAll(superMethods);
        return null;
    }

    @Override
    public TypeCompileType getSuperclass() {
        return superclass;
    }

    private JavacTypeCompileType initSuperclass() {
        if (element == null) {
            return null;
        }
        TypeMirror superclass = element.getSuperclass();
        if (superclass.getKind() == TypeKind.NONE) {
            return null;
        }
        return new JavacTypeCompileType(
                superclass,
                (TypeElement) ((DeclaredType) superclass).asElement(),
                processEnv
        );
    }

    @Override
    public List<TypeCompileType> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    private List<JavacTypeCompileType> initInterfaces() {
        List<JavacTypeCompileType> res = new ArrayList<>();
        if (element == null) {
            return res;
        }
        List<? extends TypeMirror> interfaces = element.getInterfaces();
        if (interfaces.isEmpty()) {
            return res;
        }
        for (TypeMirror anInterface : interfaces) {
            res.add(new JavacTypeCompileType(
                    anInterface,
                    (TypeElement) ((DeclaredType) anInterface).asElement(),
                    processEnv
            ));
        }
        return res;
    }

    private List<MethodCompileType> getAllMethods(TypeElement element) {
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        List<MethodCompileType> methodElements = new ArrayList<>();

        for (Element e : enclosedElements) {
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement methodElement = (ExecutableElement) e;
            TypeMirror enclosingElementMirror = methodElement.getEnclosingElement().asType();
            if (!(enclosingElementMirror instanceof DeclaredType)) {
                continue;
            }
            ExecutableType executableType = TypeUtils.asExecutable(methodElement.asType());
            MethodCompileType methodCompileType = new JavacMethodCompileType(
                    executableType, methodElement,
                    this, processEnv);
            methodElements.add(methodCompileType);
        }
        return methodElements;
    }

    private List<MethodCompileType> deduplicateMethods(List<MethodCompileType> methods) {
        Set<String> methodSignatures = new HashSet<>();
        List<MethodCompileType> result = new ArrayList<>();
        for (MethodCompileType methodCompileType : methods) {
            String signature = methodCompileType.getSignature();
            if (methodSignatures.contains(signature)) {
                continue;
            }
            result.add(methodCompileType);
            methodSignatures.add(signature);
        }
        return result;
    }


    private List<MethodCompileType> getSuperClassesMethods(
            List<MethodCompileType> methodsIn) {
        List<TypeMirror> superClasses = getAllSuperClassesAndInterfaces(
                this.getElement());
        return getUnimplementedMethods(methodsIn, superClasses);
    }

    private List<MethodCompileType> getUnimplementedMethods(
            List<MethodCompileType> implementedMethods,
            List<TypeMirror> superClasses) {
        List<MethodCompileType> unimplementedMethods = new ArrayList<>();
        DeclaredType declaredType = (DeclaredType) this.getElement().asType();

        for (TypeMirror superClass : superClasses) {
            DeclaredType superDeclaredType = (DeclaredType) superClass;
            TypeElement superElement = (TypeElement) superDeclaredType.asElement();
            List<MethodCompileType> superMethods = getAllMethods(superElement);
            List<MethodCompileType> extracted = extractUnimplementedMethods(
                    implementedMethods,
                    declaredType,
                    superMethods);
            unimplementedMethods.addAll(extracted);
        }
        return unimplementedMethods;
    }

    private List<MethodCompileType> extractUnimplementedMethods(
            List<MethodCompileType> implementedMethods,
            DeclaredType declaredType,
            List<MethodCompileType> superMethods) {
        List<MethodCompileType> unimplementedMethods = new ArrayList<>();
        for (MethodCompileType superMethod : superMethods) {
            ExecutableType executableType = (ExecutableType)
                    processEnv.getTypeUtils().asMemberOf(declaredType, superMethod.getElement());
            if (isMethodImplemented(implementedMethods, superMethod)) {
                continue;
            }
            MethodCompileType methodCompileType =
                    new JavacMethodCompileType(
                            executableType,
                            superMethod.getElement(),
                            this,
                            processEnv
                    );
            unimplementedMethods.add(methodCompileType);
        }
        return unimplementedMethods;
    }

    private boolean isMethodImplemented(List<MethodCompileType> implementedMethods,
                                        MethodCompileType superMethod) {
        for (MethodCompileType implementedMethod : implementedMethods) {
            if (processEnv.getElementUtils().overrides(
                    superMethod.getElement(),
                    implementedMethod.getElement(),
                    this.getElement())) {
                return true;
            }
        }
        return false;
    }

    private List<TypeMirror> getAllSuperClassesAndInterfaces(TypeElement element) {
        List<TypeMirror> superClasses = getInterfacesOrSuperClass(element);
        if (superClasses.isEmpty()) {
            return superClasses;
        }
        List<TypeMirror> res = new ArrayList<>(superClasses);
        for (TypeMirror superClass : superClasses) {
            res.addAll(getAllSuperClassesAndInterfaces(
                    (TypeElement) ((DeclaredType) superClass)
                            .asElement())
            );
        }
        return res.stream().distinct().collect(Collectors.toList());
    }

    private List<TypeMirror> getInterfacesOrSuperClass(TypeElement element) {
        List<TypeMirror> interfaces = new ArrayList<>();
        TypeMirror superClass = element.getSuperclass();
        if (superClass.getKind() != TypeKind.NONE) {
            interfaces.add(superClass);
        }
        interfaces.addAll(element.getInterfaces());
        return interfaces;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return element.getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return element.getAnnotationsByType(annotationType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeCompileType)) return false;
        TypeCompileType that = (TypeCompileType) o;
        return Objects.equals(typeMirror, that.getTypeMirror())
                && Objects.equals(element, that.getElement());
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeMirror, element);
    }

    @Override
    public String toString() {
        return "JavacTypeCompileType{" +
                "typeMirror=" + typeMirror +
                ", element=" + element +
                '}';
    }

    private static final TypeCompileType INVALID =
            new JavacTypeCompileType(null, null, null);

    public static TypeCompileType invalid() {
        return INVALID;
    }
}
