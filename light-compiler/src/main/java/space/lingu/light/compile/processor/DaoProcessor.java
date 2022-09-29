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
import space.lingu.light.*;
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.Dao;
import space.lingu.light.compile.struct.*;
import space.lingu.light.compile.writer.ClassWriter;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Process a class annotated with {@link space.lingu.light.Dao}.
 *
 * @author RollW
 */
public class DaoProcessor implements Processor<Dao> {
    private final TypeElement mDaoElement;
    private final ProcessEnv mEnv;
    private final Configurations mConfigurations;

    public static final List<Class<? extends Annotation>> sHandleAnnotations =
            Arrays.asList(Insert.class, Query.class, Delete.class, Update.class);

    public DaoProcessor(TypeElement daoElement,
                        ProcessEnv env,
                        Configurations configurations) {
        mDaoElement = daoElement;
        mEnv = env;
        mConfigurations = configurations;
    }

    @Override
    public Dao process() {
        final String packageName = ElementUtil.getPackage(mDaoElement).getQualifiedName().toString();
        final String simpleName = mDaoElement.getSimpleName().toString() + ClassWriter.CLASS_SUFFIX;
        ClassName implClassName = ClassName.get(packageName, simpleName);

        Methods methods = dispatchProcessMethod();
        return new Dao(
                mDaoElement,
                simpleName,
                implClassName,
                implClassName.simpleName(),
                methods.insertMethods,
                methods.updateMethods,
                methods.deleteMethods,
                methods.queryMethods,
                methods.transactionMethods,
                mConfigurations);
    }

    private Methods dispatchProcessMethod() {
        List<ExecutableElement> allMethods = getAllMethods(mDaoElement);
        List<QueryMethod> queryMethods = new ArrayList<>();
        List<DeleteMethod> deleteMethods = new ArrayList<>();
        List<UpdateMethod> updateMethods = new ArrayList<>();
        List<InsertMethod> insertMethods = new ArrayList<>();
        List<TransactionMethod> transactionMethods = new ArrayList<>();

        Map<Class<? extends Annotation>, List<ExecutableElement>> methods = new HashMap<>();

        boolean isInterface = mDaoElement.getKind() == ElementKind.INTERFACE;

        sHandleAnnotations.forEach(anno ->
                methods.put(anno, new ArrayList<>()));

        allMethods.forEach(method -> {
            if ((isInterface && !ElementUtil.isDefault(method)) ||
                    ElementUtil.isAbstract(method)) {
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

        methods.get(Query.class).forEach(method ->
                addNonNull(queryMethods, processQueryMethod(method)));
        methods.get(Delete.class).forEach(method ->
                addNonNull(deleteMethods, processDeleteMethod(method)));
        methods.get(Update.class).forEach(method ->
                addNonNull(updateMethods, processUpdateMethod(method)));
        methods.get(Insert.class).forEach(method ->
                addNonNull(insertMethods, processInsertMethod(method)));

        allMethods.forEach(element -> {
            if (element.getAnnotation(Transaction.class) == null) {
                return;
            }
            for (Class<? extends Annotation> annotation : sHandleAnnotations) {
                if (element.getAnnotation(annotation) != null) {
                    return;
                }
            }
            if (isInterface) {
                if (!ElementUtil.isDefault(element)) {
                    mEnv.getLog().error(CompileErrors.TRANSACTION_METHOD_NOT_DEFAULT, element);
                    return;
                }
            } else {
                if (ElementUtil.isAbstract(element)) {
                    mEnv.getLog().error(CompileErrors.TRANSACTION_METHOD_ABSTRACT, element);
                    return;
                }
            }
            transactionMethods.add(processTransactionMethod(element));
        });
        return new Methods(insertMethods,
                updateMethods, deleteMethods,
                queryMethods, transactionMethods);
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

    private QueryMethod processQueryMethod(ExecutableElement methodElement) {
        Processor<QueryMethod> processor = new QueryMethodProcessor(methodElement, mDaoElement, mEnv);
        return processor.process();
    }

    private DeleteMethod processDeleteMethod(ExecutableElement methodElement) {
        Processor<DeleteMethod> processor = new DeleteMethodProcessor(methodElement, mDaoElement, mEnv);
        return processor.process();
    }

    private InsertMethod processInsertMethod(ExecutableElement methodElement) {
        Processor<InsertMethod> processor = new InsertMethodProcessor(methodElement, mDaoElement, mEnv);
        return processor.process();
    }

    private UpdateMethod processUpdateMethod(ExecutableElement methodElement) {
        Processor<UpdateMethod> processor = new UpdateMethodProcessor(methodElement, mDaoElement, mEnv);
        return processor.process();
    }

    private TransactionMethod processTransactionMethod(ExecutableElement methodElement) {
        Processor<TransactionMethod> processor = new TransactionMethodProcessor(methodElement, mDaoElement, mEnv);
        return processor.process();
    }

    private List<ExecutableElement> getAllMethods(TypeElement element) {
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        List<ExecutableElement> methodElements = new ArrayList<>();
        for (Element e : enclosedElements) {
            if (e.getKind() != ElementKind.METHOD) continue;
            ExecutableElement methodElement = (ExecutableElement) e;
            methodElements.add(methodElement);

        }
        return methodElements;
    }


}
