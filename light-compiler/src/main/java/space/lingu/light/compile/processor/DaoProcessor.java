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
import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.*;
import space.lingu.light.compile.struct.Dao;
import space.lingu.light.compile.writer.ClassWriter;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 处理一个有{@link space.lingu.light.Dao}注解的类
 * @author RollW
 */
public class DaoProcessor implements Processor<Dao> {
    private final TypeElement mDaoElement;
    private final ProcessEnv mEnv;
    private final Dao dao = new Dao();

    public static final List<Class<? extends Annotation>> PROCESS_ANNOTATIONS =
            Arrays.asList(Insert.class, Query.class, Delete.class, Update.class);

    public DaoProcessor(TypeElement daoElement, ProcessEnv env) {
        mDaoElement = daoElement;
        mEnv = env;
    }

    @Override
    public Dao process() {
        final String packageName = ElementUtil.getPackage(mDaoElement).getQualifiedName().toString();
        ClassName implClassName = ClassName.get(packageName,
                mDaoElement.getSimpleName().toString() + ClassWriter.CLASS_SUFFIX);
        dao.setElement(mDaoElement)
                .setSimpleName(mDaoElement.getSimpleName().toString())
                .setImplName(implClassName.simpleName())
                .setImplClassName(implClassName);

        dispatchProcessMethod();
        return dao;
    }

    private void dispatchProcessMethod() {
        List<ExecutableElement> allMethods = getAllMethods(mDaoElement);
        List<QueryMethod> queryMethods = new ArrayList<>();
        List<DeleteMethod> deleteMethods = new ArrayList<>();
        List<UpdateMethod> updateMethods = new ArrayList<>();
        List<InsertMethod> insertMethods = new ArrayList<>();
        List<TransactionMethod> transactionMethods = new ArrayList<>();

        Map<Class<? extends Annotation>, List<ExecutableElement>> methods = new HashMap<>();

        boolean isInterface = mDaoElement.getKind() == ElementKind.INTERFACE;

        PROCESS_ANNOTATIONS.forEach(anno ->
                methods.put(anno, new ArrayList<>()));

        allMethods.forEach(method -> {
            if ((isInterface && !ElementUtil.isDefault(method)) ||
                    ElementUtil.isAbstract(method)) {
                AtomicBoolean annotatedFlag = new AtomicBoolean(false);
                PROCESS_ANNOTATIONS.forEach(anno -> {
                    if (method.getAnnotation(anno) != null) {
                        methods.get(anno).add(method);
                        annotatedFlag.set(true);
                    }
                });
                if (!annotatedFlag.get()) {
                    throw new LightCompileException("An abstract method must be annotated with one of the annotations below: \n" +
                            "@Insert, @Delete, @Update, @Query");
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
            for (Class<? extends Annotation> processAnnotation : PROCESS_ANNOTATIONS) {
                if (element.getAnnotation(processAnnotation) != null) {
                    return;
                }
            }
            if (isInterface) {
                if (!ElementUtil.isDefault(element)) {
                    throw new LightCompileException("The transaction method in an interface must have a default implementation.");
                }
            } else {
                if (ElementUtil.isAbstract(element)) {
                    throw new LightCompileException("The transaction method cannot be abstract.");
                }
            }
            transactionMethods.add(processTransactionMethod(element));
        });

        dao.setQueryMethods(queryMethods)
                .setInsertMethods(insertMethods)
                .setDeleteMethods(deleteMethods)
                .setUpdateMethods(updateMethods)
                .setTransactionMethods(transactionMethods);
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
