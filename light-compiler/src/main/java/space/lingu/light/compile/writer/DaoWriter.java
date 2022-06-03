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

package space.lingu.light.compile.writer;

import com.squareup.javapoet.*;
import space.lingu.light.DaoConnectionGetter;
import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.JavaPoetClass;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeUtil;
import space.lingu.light.compile.struct.*;
import space.lingu.light.util.Pair;
import space.lingu.light.util.StringUtil;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 写入{@code Dao_Impl}类中
 * @author RollW
 */
public class DaoWriter extends ClassWriter {
    private final Dao mDao;
    private final TypeElement dbElement;
    public static final FieldSpec DATABASE_FIELD = FieldSpec.builder(JavaPoetClass.LIGHT_DATABASE,
            "__db", Modifier.FINAL, Modifier.PRIVATE).build();

    public DaoWriter(Dao dao, TypeElement dbElement, ProcessEnv env) {
        super(dao.getImplClassName(), env);
        this.dbElement = dbElement;
        mDao = dao;
    }

    @Override
    protected TypeSpec.Builder createTypeSpecBuilder() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(mDao.getImplClassName())
                .addOriginatingElement(dbElement)
                .addField(DATABASE_FIELD)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        ParameterSpec dbParam = ParameterSpec.builder(JavaPoetClass.LIGHT_DATABASE, "db")
                .build();

        List<Pair<MethodSpec, Pair<QueryMethod, FieldSpec>>> queryMethods = createQueryMethods();

        List<MethodPair> methodList = new ArrayList<>();
        methodList.addAll(createInsertMethods());
        methodList.addAll(createDeleteMethods());
        methodList.addAll(createUpdateMethods());

        methodList.forEach(mapMethodSpecPair ->
                builder.addMethod(mapMethodSpecPair.methodImpl));

        queryMethods.forEach(pair -> builder.addMethod(pair.first));

        mDao.getTransactionMethods().forEach(method ->
                builder.addMethod(createTransactionMethodBody(method)));

        boolean callSuper;


        if (ElementUtil.isInterface(mDao.getElement())) {
            builder.addSuperinterface(ClassName.get(mDao.getElement()))
                    .addMethod(createConstructor(dbParam, methodList, queryMethods, new ConstructorConf(false, false)));
        } else {
            builder.superclass(ClassName.get(mDao.getElement()))
                    .addMethod(createConstructor(dbParam, methodList, queryMethods, checkConstructorCallSuper()));// TODO 识别父类构造函数参数
        }

        if (checkConnectionGetterInterface(mDao)) {
            builder.addMethod(createGetConnectionMethod());
        }

        return builder;
    }

    private MethodSpec createGetConnectionMethod() {
        return MethodSpec.methodBuilder("getConnection")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(JavaPoetClass.SHARED_CONNECTION)
                .addAnnotation(Override.class)
                .addStatement("return new $T($N)", JavaPoetClass.SHARED_CONNECTION, DATABASE_FIELD.name)
                .build();
    }

    private boolean checkConnectionGetterInterface(Dao dao) {
        for (TypeMirror anInterface : dao.getElement().getInterfaces()) {
            TypeElement element = ElementUtil.asTypeElement(anInterface);
            if (element!= null && element.getQualifiedName()
                    .contentEquals(DaoConnectionGetter.class.getCanonicalName())) {
                return true;
            }
        }
        return false;
    }
    private static class ConstructorConf {
        boolean callSuper;
        boolean paramEmpty;

        ConstructorConf(boolean callSuper, boolean paramEmpty) {
            this.callSuper = callSuper;
            this.paramEmpty = paramEmpty;
        }
    }

    private ConstructorConf checkConstructorCallSuper() {
        List<? extends Element> elements = mDao.getElement().getEnclosedElements();
        List<ExecutableElement> constructors = new ArrayList<>();
        boolean isSuper = false, isEmpty = false;
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CONSTRUCTOR) {
                continue;
            }
            constructors.add((ExecutableElement) element);
        }

        if (constructors.size() > 1) {
            throw new LightCompileException("Only can have one constructor that is parameterless or have a Database parameter.");
        }
        TypeElement lightDatabaseElement = mEnv.getElementUtils()
                .getTypeElement(JavaPoetClass.LIGHT_DATABASE.canonicalName());

        for (ExecutableElement constructor : constructors) {
            isSuper = true;
            List<? extends VariableElement> params = constructor.getParameters();
            if (params.isEmpty()) {
                isEmpty = true;
                break;
            }
            if (params.size() > 1) {
                throw new LightCompileException("One constructor in DAO can only have one Database parameter or is parameterless.");
            }
            for (VariableElement param : params) {
                if (!ElementUtil.equalTypeElement(dbElement, ElementUtil.asTypeElement(param.asType())) &&
                        !ElementUtil.equalTypeElement(dbElement, lightDatabaseElement)) {
                    throw new LightCompileException("Parameter must be of type LightDatabase.");
                }
            }
        }
        return new ConstructorConf(isSuper, isEmpty);
    }

    private MethodSpec createConstructor(ParameterSpec param,
                                         List<MethodPair> methodPairs,
                                         List<Pair<MethodSpec, Pair<QueryMethod, FieldSpec>>> queryPairs,
                                         ConstructorConf conf) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(param);
        if (conf.callSuper) {
            if (conf.paramEmpty) {
                builder.addStatement("super()");
            } else {
                builder.addStatement("super($N)", param);
            }
        }
        builder.addStatement("this.$N = $N", DATABASE_FIELD, param);
        Set<Pair<FieldSpec, TypeSpec>> set = new HashSet<>();
        methodPairs.stream()
                .filter(methodPair -> !methodPair.fields.isEmpty())
                .forEach(methodPair ->{
                    AtomicReference<Pair<FieldSpec, TypeSpec>> pair = new AtomicReference<>();
                    methodPair.fields.values().stream()
                            .filter(specPair -> specPair.first != null && specPair.second != null)
                            .forEach(pair::set);
                    set.add(pair.get());// 去重
                });
        set.forEach(pair -> builder.addStatement("this.$N = $L",
                pair.first, pair.second));
        queryPairs.forEach(pair -> {
            StringJoiner argOrderJoiner = new StringJoiner(", ");
            pair.second.first.getParameters().forEach(queryParameter -> {
                argOrderJoiner.add("\"" + queryParameter.getName() + "\"");
            });
            final String argOrder = pair.second.first.getParameters().isEmpty() ? "" :
                    ", " + argOrderJoiner;

            builder.addStatement("this.$N = new $T($L, $S$L)", pair.second.second,
                    JavaPoetClass.QUERY_HANDLER,
                    DATABASE_FIELD.name,
                    pair.second.first.getQuery(), argOrder);
        });
        return builder.build();
    }

    private List<MethodPair> createDeleteMethods() {
        List<MethodPair> pairList = new ArrayList<>();
        mDao.getDeleteMethods().forEach(method -> {
            final Map<String, Pair<FieldSpec, TypeSpec>> fields = new HashMap<>();
            method.getEntities().forEach((s, paramEntity) -> {
                fields.put(s,
                        Pair.createPair(getOrCreateField(new DeleteUpdateMethodField("delete", paramEntity)),
                                new DeleteHandlerWriter(paramEntity).createAnonymous(this, DATABASE_FIELD.name)));
            });
            MethodSpec methodImpl = MethodSpec.overriding(method.getElement())
                    .addModifiers(Modifier.FINAL)
                    .addCode(createAnnotatedMethodBody(method, fields))
                    .build();
            pairList.add(new MethodPair(fields, methodImpl));
        });
        return pairList;
    }

    private List<MethodPair> createUpdateMethods() {
        List<MethodPair> pairList = new ArrayList<>();
        mDao.getUpdateMethods().forEach(method -> {
            final Map<String, Pair<FieldSpec, TypeSpec>> fields = new HashMap<>();
            method.getEntities().forEach((s, paramEntity) -> {
                fields.put(s,
                        Pair.createPair(getOrCreateField(new DeleteUpdateMethodField("update", paramEntity)),
                                new UpdateHandlerWriter(paramEntity).createAnonymous(this, DATABASE_FIELD.name)));
            });
            MethodSpec methodImpl = MethodSpec.overriding(method.getElement())
                    .addModifiers(Modifier.FINAL)
                    .addCode(createAnnotatedMethodBody(method, fields))
                    .build();
            pairList.add(new MethodPair(fields, methodImpl));
        });

        return pairList;
    }

    private List<MethodPair> createInsertMethods() {
        List<MethodPair> pairList = new ArrayList<>();
        mDao.getInsertMethods().forEach(method -> {
            final Map<String, Pair<FieldSpec, TypeSpec>> fields = new HashMap<>();
            method.getEntities().forEach((s, paramEntity) -> {
                fields.put(s,
                        Pair.createPair(getOrCreateField(new InsertMethodField(paramEntity)),
                                new InsertHandlerWriter(paramEntity).createAnonymous(this, DATABASE_FIELD.name)));
            });
            MethodSpec methodImpl = MethodSpec.overriding(method.getElement())
                    .addModifiers(Modifier.FINAL)
                    .addCode(createAnnotatedMethodBody(method, fields))
                    .build();
            pairList.add(new MethodPair(fields, methodImpl));
        });
        return pairList;
    }

    private CodeBlock createAnnotatedMethodBody(AnnotatedMethod method,
                                                Map<String, Pair<FieldSpec, TypeSpec>> fields) {
        if (fields.isEmpty()) {
            return CodeBlock.builder().build();
        }

        GenerateCodeBlock block = new GenerateCodeBlock(this);
        method.getBinder().writeBlock(method.getParameters(), fields, block);
        return block.generate();
    }

    private List<Pair<MethodSpec, Pair<QueryMethod, FieldSpec>>> createQueryMethods() {
        List<Pair<MethodSpec, Pair<QueryMethod, FieldSpec>>> pairList = new ArrayList<>();
        mDao.getQueryMethods().forEach(method -> {
            Pair<QueryMethod, FieldSpec> fieldSpecPair = Pair.createPair(method,
                    getOrCreateField(new QueryHandlerField(method)));
            MethodSpec methodImpl = MethodSpec.overriding(method.getElement())
                    .addModifiers(Modifier.FINAL)
                    .addCode(createQueryMethodBody(fieldSpecPair))
                    .build();
            pairList.add(Pair.createPair(methodImpl, fieldSpecPair));
        });

        return pairList;
    }

    private MethodSpec createTransactionMethodBody(TransactionMethod method) {
        GenerateCodeBlock block = new GenerateCodeBlock(this);
        method.getBinder().writeBlock(method.getReturnType(), method.getParamNames(),
                mDao.getClassName(), mDao.getImplClassName(), block);
        return MethodSpec.overriding(method.getElement())
                .addCode(block.generate())
                .build();
    }

    private CodeBlock createQueryMethodBody(Pair<QueryMethod, FieldSpec> field) {
        QueryMethodWriter writer = new QueryMethodWriter(field.first);
        GenerateCodeBlock block = new GenerateCodeBlock(this);
        final String sqlVar = block.getTempVar("_sql");
        final String stmtVar = block.getTempVar("_stmt");
        writer.prepare(stmtVar, field.second.name, block);

        field.first.getBinder()
                .writeBlock(field.second.name, stmtVar, true,
                        !TypeUtil.isVoid(field.first.getReturnType()),
                        field.first.isTransaction(), block);
        return block.generate();
    }

    private static String identifierParamNameAndType(List<QueryParameter> parameters) {
        StringBuilder builder = new StringBuilder();
        parameters.forEach(parameter -> {
            builder.append(StringUtil.firstUpperCase(parameter.getName()));
            TypeElement element = ElementUtil.asTypeElement(parameter.getTypeMirror());
            if (element != null) {
                builder.append(element.getSimpleName().toString());
            } else {
                builder.append(StringUtil.firstUpperCase(parameter.getTypeMirror().toString()));
            }
        });
        return builder.toString();
    }

    private static String entityFieldName(ParamEntity entity) {
        if (entity.isPartialEntity()) {
            return typeNameToFieldName(entity.getPojo().getTypeName()) + "As" +
                    typeNameToFieldName(entity.getDataTable().getTypeName());
        }
        return typeNameToFieldName(entity.getPojo().getTypeName());
    }

    private static String typeNameToFieldName(TypeName typeName) {
        if (typeName instanceof ClassName) {
            return ((ClassName) typeName).simpleName();
        }
        return typeName.toString().replace('.', '_');
    }

    private static class QueryHandlerField extends SharedFieldSpec {
        private final String sql;

        private QueryHandlerField(QueryMethod method) {
            super("queryHandlerOf" + StringUtil
                            .firstUpperCase(method.getElement().getSimpleName().toString())
                            + identifierParamNameAndType(method.getParameters()),
                    JavaPoetClass.QUERY_HANDLER);
            this.sql = method.getQuery();
        }

        @Override
        String getUniqueKey() {
            return "-QueryHandler-" + sql;
        }

        @Override
        void prepare(ClassWriter writer, FieldSpec.Builder builder) {
            builder.addModifiers(Modifier.PRIVATE, Modifier.FINAL);
        }

    }

    private static class DeleteUpdateMethodField extends SharedFieldSpec {
        private final ParamEntity entity;
        private final String prefix;
        DeleteUpdateMethodField(String prefix, ParamEntity entity) {
            super(prefix + "_deleteUpdateHandlerOf" + entityFieldName(entity),
                    ParameterizedTypeName.get(JavaPoetClass.DELETE_UPDATE_HANDLER,
                    entity.getPojo().getTypeName()));
            this.prefix = prefix;
            this.entity = entity;
        }

        @Override
        String getUniqueKey() {
            return prefix + "DeleteUpdateHandler-" + entity.getPojo().getTypeName() + "-" + entity.getTableName();
        }

        @Override
        void prepare(ClassWriter writer, FieldSpec.Builder builder) {
            builder.addModifiers(Modifier.FINAL, Modifier.PRIVATE);
        }
    }

    private static class InsertMethodField extends SharedFieldSpec {
        // TODO OnConflict
        private final ParamEntity entity;

        InsertMethodField(ParamEntity entity) {
            super("insertHandlerOf" + entityFieldName(entity),
                    ParameterizedTypeName.get(JavaPoetClass.INSERT_HANDLER,
                    entity.getPojo().getTypeName()));
            this.entity = entity;
        }

        @Override
        String getUniqueKey() {
            return "InsertHandler-" + entity.getPojo().getTypeName() + "-" + entity.getTableName();
        }

        @Override
        void prepare(ClassWriter writer, FieldSpec.Builder builder) {
            builder.addModifiers(Modifier.FINAL, Modifier.PRIVATE);
        }
    }

    private static class MethodPair {
        final Map<String, Pair<FieldSpec, TypeSpec>> fields;
        final MethodSpec methodImpl;

        public MethodPair(Map<String, Pair<FieldSpec, TypeSpec>> fields,
                          MethodSpec methodImpl) {
            this.fields = fields;
            this.methodImpl = methodImpl;
        }
    }


}
