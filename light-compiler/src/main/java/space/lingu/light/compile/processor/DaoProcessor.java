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

package space.lingu.light.compile.processor;

import com.squareup.javapoet.ClassName;
import space.lingu.light.Configurations;
import space.lingu.light.DaoConnectionGetter;
import space.lingu.light.Delete;
import space.lingu.light.Insert;
import space.lingu.light.Query;
import space.lingu.light.Transaction;
import space.lingu.light.Update;
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.javac.TypeUtil;
import space.lingu.light.compile.javac.types.JavacMethodCompileType;
import space.lingu.light.compile.struct.Dao;
import space.lingu.light.compile.struct.DeleteMethod;
import space.lingu.light.compile.struct.InsertMethod;
import space.lingu.light.compile.struct.QueryMethod;
import space.lingu.light.compile.struct.TransactionMethod;
import space.lingu.light.compile.struct.UpdateMethod;
import space.lingu.light.compile.writer.ClassWriter;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Process a class annotated with {@link space.lingu.light.Dao}.
 *
 * @author RollW
 */
public class DaoProcessor implements Processor<Dao> {
    private final TypeCompileType typeCompileType;
    private final ProcessEnv mEnv;
    private final Configurations mConfigurations;

    public static final List<Class<? extends Annotation>> sHandleAnnotations =
            Arrays.asList(Insert.class, Query.class, Delete.class, Update.class);

    public DaoProcessor(TypeCompileType typeCompileType,
                        ProcessEnv env,
                        Configurations configurations) {
        this.typeCompileType = typeCompileType;
        mEnv = env;
        mConfigurations = configurations;
    }

    @Override
    public Dao process() {
        final String packageName = ElementUtil
                .getPackage(typeCompileType.getElement())
                .getQualifiedName()
                .toString();
        final String simpleName = typeCompileType.getSimpleName().toString() + ClassWriter.CLASS_SUFFIX;
        ClassName implClassName = ClassName.get(packageName, simpleName);

        Methods methods = dispatchProcessMethod();
        return new Dao(
                typeCompileType,
                simpleName,
                implClassName,
                implClassName.simpleName(),
                methods.insertMethods,
                methods.updateMethods,
                methods.deleteMethods,
                methods.queryMethods,
                methods.transactionMethods,
                mConfigurations
        );
    }

    private Methods dispatchProcessMethod() {
        List<MethodCompileType> daoMethods = getAllMethods(typeCompileType.getElement());
        List<MethodCompileType> superMethods = getSuperClassesMethods(daoMethods);
        daoMethods.addAll(superMethods);

        List<MethodCompileType> allMethods =
                deduplicateMethods(daoMethods);

        List<QueryMethod> queryMethods = new ArrayList<>();
        List<DeleteMethod> deleteMethods = new ArrayList<>();
        List<UpdateMethod> updateMethods = new ArrayList<>();
        List<InsertMethod> insertMethods = new ArrayList<>();
        List<TransactionMethod> transactionMethods = new ArrayList<>();

        Map<Class<? extends Annotation>, List<MethodCompileType>> methods = new HashMap<>();

        boolean isInterface = typeCompileType.getElement().getKind() == ElementKind.INTERFACE;

        sHandleAnnotations.forEach(anno ->
                methods.put(anno, new ArrayList<>()));

        checkMethodsAnnotation(allMethods, methods, isInterface);

        methods.get(Query.class).forEach(method ->
                addNonNull(queryMethods, processQueryMethod(method)));
        methods.get(Delete.class).forEach(method ->
                addNonNull(deleteMethods, processDeleteMethod(method)));
        methods.get(Update.class).forEach(method ->
                addNonNull(updateMethods, processUpdateMethod(method)));
        methods.get(Insert.class).forEach(method ->
                addNonNull(insertMethods, processInsertMethod(method)));

        checkTransactionMethods(allMethods, transactionMethods, isInterface);

        return new Methods(
                insertMethods,
                updateMethods,
                deleteMethods,
                queryMethods,
                transactionMethods
        );
    }

    private void checkTransactionMethods(List<MethodCompileType> allMethods, List<TransactionMethod> transactionMethods, boolean isInterface) {
        allMethods.forEach(methodCompileType -> {
            if (methodCompileType.getAnnotation(Transaction.class) == null) {
                return;
            }
            for (Class<? extends Annotation> annotation : sHandleAnnotations) {
                if (methodCompileType.getAnnotation(annotation) != null) {
                    return;
                }
            }
            if (isInterface) {
                if (!ElementUtil.isDefault(methodCompileType.getElement())) {
                    mEnv.getLog().error(CompileErrors.TRANSACTION_METHOD_NOT_DEFAULT, methodCompileType);
                    return;
                }
            } else {
                if (ElementUtil.isAbstract(methodCompileType.getElement())) {
                    mEnv.getLog().error(CompileErrors.TRANSACTION_METHOD_ABSTRACT, methodCompileType);
                    return;
                }
            }
            transactionMethods.add(processTransactionMethod(methodCompileType));
        });
    }

    private void checkMethodsAnnotation(List<MethodCompileType> allMethods,
                                        Map<Class<? extends Annotation>, List<MethodCompileType>> methods,
                                        boolean isInterface) {
        allMethods.forEach(method -> {
            if ((isInterface && !ElementUtil.isDefault(method.getElement())) ||
                    ElementUtil.isAbstract(method.getElement())) {
                AtomicBoolean annotatedFlag = new AtomicBoolean(false);
                sHandleAnnotations.forEach(anno -> {
                    if (method.getAnnotation(anno) != null) {
                        methods.get(anno).add(method);
                        annotatedFlag.set(true);
                    }
                });
                if (!annotatedFlag.get()) {
                    mEnv.getLog().error(
                            CompileErrors.DAO_INVALID_ABSTRACT_METHOD,
                            method
                    );
                }
            }
        });
    }

    private static final class Methods {
        final List<InsertMethod> insertMethods;
        final List<UpdateMethod> updateMethods;
        final List<DeleteMethod> deleteMethods;
        final List<QueryMethod> queryMethods;
        final List<TransactionMethod> transactionMethods;

        private Methods(List<InsertMethod> insertMethods,
                        List<UpdateMethod> updateMethods,
                        List<DeleteMethod> deleteMethods,
                        List<QueryMethod> queryMethods,
                        List<TransactionMethod> transactionMethods) {
            this.insertMethods = insertMethods;
            this.updateMethods = updateMethods;
            this.deleteMethods = deleteMethods;
            this.queryMethods = queryMethods;
            this.transactionMethods = transactionMethods;
        }
    }

    private static <T> void addNonNull(List<T> list, T t) {
        if (t != null) {
            list.add(t);
        }
    }

    private QueryMethod processQueryMethod(MethodCompileType methodElement) {
        Processor<QueryMethod> processor = new QueryMethodProcessor(methodElement, typeCompileType, mEnv);
        return processor.process();
    }

    private DeleteMethod processDeleteMethod(MethodCompileType methodElement) {
        Processor<DeleteMethod> processor = new DeleteMethodProcessor(methodElement, typeCompileType, mEnv);
        return processor.process();
    }

    private InsertMethod processInsertMethod(MethodCompileType methodElement) {
        Processor<InsertMethod> processor = new InsertMethodProcessor(methodElement, typeCompileType, mEnv);
        return processor.process();
    }

    private UpdateMethod processUpdateMethod(MethodCompileType methodElement) {
        Processor<UpdateMethod> processor = new UpdateMethodProcessor(methodElement, typeCompileType, mEnv);
        return processor.process();
    }

    private TransactionMethod processTransactionMethod(MethodCompileType methodElement) {
        Processor<TransactionMethod> processor = new TransactionMethodProcessor(methodElement, typeCompileType, mEnv);
        return processor.process();
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
            ExecutableType executableType = TypeUtil.asExecutable(methodElement.asType());
            MethodCompileType methodCompileType = new JavacMethodCompileType(
                    executableType, methodElement,
                    this.typeCompileType);
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
            List<MethodCompileType> methodsInDao) {
        List<TypeMirror> superClasses = getAllSuperClassesAndInterfaces(
                typeCompileType.getElement());
        return getUnimplementedMethods(methodsInDao, superClasses);
    }

    private List<MethodCompileType> getUnimplementedMethods(
            List<MethodCompileType> implementedMethods,
            List<TypeMirror> superClasses) {
        List<MethodCompileType> unimplementedMethods = new ArrayList<>();
        DeclaredType declaredType = (DeclaredType) typeCompileType.getElement().asType();

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
                    mEnv.getTypeUtils().asMemberOf(declaredType, superMethod.getElement());
            if (isMethodImplemented(implementedMethods, superMethod)) {
                continue;
            }
            MethodCompileType methodCompileType =
                    new JavacMethodCompileType(
                            executableType,
                            superMethod.getElement(),
                            this.typeCompileType
                    );
            unimplementedMethods.add(methodCompileType);
        }
        return unimplementedMethods;
    }

    private boolean isMethodImplemented(List<MethodCompileType> implementedMethods,
                                        MethodCompileType superMethod) {
        for (MethodCompileType implementedMethod : implementedMethods) {
            if (mEnv.getElementUtils().overrides(
                    superMethod.getElement(),
                    implementedMethod.getElement(),
                    typeCompileType.getElement())) {
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
        TypeMirror daoConnectionGetter = mEnv.getElementUtils()
                .getTypeElement(DaoConnectionGetter.class.getCanonicalName())
                .asType();
        List<? extends TypeMirror> unprocessedInterfaces = element.getInterfaces();
        // needs to exclude DaoConnectionGetter interface
        for (TypeMirror unprocessedInterface : unprocessedInterfaces) {
            if (unprocessedInterface.equals(daoConnectionGetter)) {
                continue;
            }
            interfaces.add(unprocessedInterface);
        }
        interfaces.addAll(element.getInterfaces());
        return interfaces;
    }
}
