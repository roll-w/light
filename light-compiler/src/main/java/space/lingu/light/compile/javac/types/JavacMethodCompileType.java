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

import space.lingu.light.compile.javac.ElementUtils;
import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.javac.VariableCompileType;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author RollW
 */
public class JavacMethodCompileType implements MethodCompileType {
    private final ExecutableType typeMirror;
    private final ExecutableElement element;
    private final TypeCompileType declaringType;

    private final List<VariableCompileType> parameters;
    private final TypeCompileType returnType;

    public JavacMethodCompileType(ExecutableType typeMirror,
                                  ExecutableElement element,
                                  TypeCompileType declaringType,
                                  ProcessEnv processEnv) {
        this.typeMirror = typeMirror;
        this.element = element;
        this.declaringType = declaringType;
        this.parameters = new ArrayList<>();

        List<? extends VariableElement> variableElements =
                element.getParameters();
        List<? extends TypeMirror> parameterTypes = typeMirror.getParameterTypes();
        for (int i = 0; i < typeMirror.getParameterTypes().size(); i++) {
            VariableElement variableElement = variableElements.get(i);
            TypeMirror parameterType = parameterTypes.get(i);
            parameters.add(new JavacVariableCompileType(
                    parameterType,
                    variableElement,
                    processEnv
            ));
        }
        returnType = new JavacTypeCompileType(
                typeMirror.getReturnType(),
                ElementUtils.asTypeElement(typeMirror.getReturnType()),
                processEnv
        );
    }

    @Override
    public ExecutableType getTypeMirror() {
        return typeMirror;
    }

    @Override
    public ExecutableElement getElement() {
        return element;
    }

    @Override
    public DeclaredType getDeclaringType() {
        return (DeclaredType) declaringType.getTypeMirror();
    }

    @Override
    public String getSignature() {
        String returnType = getReturnType().getName();
        String methodName = getName();
        StringJoiner joiner = new StringJoiner(",", "(", ")");
        for (VariableCompileType parameter : getParameters()) {
            joiner.add(parameter.getTypeMirror().toString());
        }
        return returnType + " " + methodName + joiner;
    }

    @Override
    public TypeCompileType getDeclaringIn() {
        return declaringType;
    }

    @Override
    public TypeCompileType getReturnType() {
        return returnType;
    }

    @Override
    public List<VariableCompileType> getParameters() {
        return parameters;
    }

    @Override
    public String getName() {
        return element.getSimpleName().toString();
    }

    @Override
    public Name getSimpleName() {
        return element.getSimpleName();
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
        if (!(o instanceof MethodCompileType)) return false;
        MethodCompileType that = (MethodCompileType) o;
        return Objects.equals(typeMirror, that.getTypeMirror()) && Objects.equals(element, that.getElement());
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeMirror, element, declaringType, parameters, returnType);
    }

    @Override
    public String toString() {
        return "JavacMethodCompileType{" +
                "typeMirror=" + typeMirror +
                ", element=" + element +
                ", declaringIn=" + declaringType +
                ", parameters=" + parameters +
                ", returnType=" + returnType +
                '}';
    }
}
