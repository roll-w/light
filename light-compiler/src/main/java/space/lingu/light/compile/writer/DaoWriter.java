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
import space.lingu.light.Configurations;
import space.lingu.light.DaoConnectionGetter;
import space.lingu.light.LightConfiguration;
import space.lingu.light.OnConflictStrategy;
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.JavaPoetClass;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeUtil;
import space.lingu.light.compile.struct.*;
import space.lingu.light.handler.SQLExpressionParser;
import space.lingu.light.util.Pair;
import space.lingu.light.util.StringUtil;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 写入{@code Dao_Impl}类中
 *
 * @author RollW
 */
public class DaoWriter extends ClassWriter {
    private final Dao mDao;
    private final TypeElement dbElement;
    public static final FieldSpec sDatabaseField = FieldSpec.builder(JavaPoetClass.LIGHT_DATABASE,
            "__db", Modifier.FINAL, Modifier.PRIVATE).build();

    public DaoWriter(Dao dao, TypeElement dbElement, ProcessEnv env) {
        super(dao.getImplClassName(), dao.getClassName(), env);
        this.dbElement = dbElement;
        mDao = dao;
    }

    @Override
    protected TypeSpec.Builder createTypeSpecBuilder() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(mDao.getImplClassName())
                .addOriginatingElement(dbElement)
                .addField(sDatabaseField)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        ParameterSpec dbParam = ParameterSpec.builder(JavaPoetClass.LIGHT_DATABASE, "db")
                .build();

        List<SQLMethodPair> sqlMethodPairs = new ArrayList<>();
        sqlMethodPairs.addAll(createQueryMethods());
        sqlMethodPairs.addAll(createCustomDeleteMethods());

        List<AutoMethodPair> autoMethodPairs = new ArrayList<>();
        autoMethodPairs.addAll(createInsertMethods());
        autoMethodPairs.addAll(createAutoDeleteMethods());
        autoMethodPairs.addAll(createUpdateMethods());

        autoMethodPairs.forEach(mapMethodSpecPair ->
                builder.addMethod(mapMethodSpecPair.methodImpl));

        sqlMethodPairs.forEach(pair ->
                builder.addMethod(pair.methodSpec));

        mDao.getTransactionMethods().forEach(method ->
                builder.addMethod(createTransactionMethodBody(method)));

        Configurations configurations = mDao.getConfigurations();

        if (ElementUtil.isInterface(mDao.getElement())) {
            builder.addSuperinterface(ClassName.get(mDao.getElement()))
                    .addMethod(createConstructor(dbParam,
                            autoMethodPairs,
                            sqlMethodPairs,
                            new ConstructorConf(false, false),
                            configurations));
        } else {
            builder.superclass(ClassName.get(mDao.getElement()))
                    .addMethod(createConstructor(dbParam,
                            autoMethodPairs,
                            sqlMethodPairs,
                            checkConstructorCallSuper(),
                            configurations));
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
                .addStatement("return new $T($N)", JavaPoetClass.SHARED_CONNECTION, sDatabaseField.name)
                .build();
    }

    private boolean checkConnectionGetterInterface(Dao dao) {
        for (TypeMirror anInterface : dao.getElement().getInterfaces()) {
            TypeElement element = ElementUtil.asTypeElement(anInterface);
            if (element != null && element.getQualifiedName()
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
            mEnv.getLog().error(
                    CompileErrors.DAO_TOO_MUCH_CONSTRUCTORS,
                    mDao.getElement()
            );
        }

        TypeElement lightDatabaseElement =
                mEnv.getElementUtils().getTypeElement(JavaPoetClass.LIGHT_DATABASE.canonicalName());

        for (ExecutableElement constructor : constructors) {
            isSuper = true;
            List<? extends VariableElement> params = constructor.getParameters();
            if (params.isEmpty()) {
                isEmpty = true;
                break;
            }
            if (params.size() > 1) {
                mEnv.getLog().error(
                        CompileErrors.DAO_CONSTRUCTOR_TOO_MUCH_PARAMS,
                        constructor
                );
            }
            for (VariableElement param : params) {
                if (!ElementUtil.equalTypeElement(lightDatabaseElement,
                        ElementUtil.asTypeElement(param.asType()))) {
                    mEnv.getLog().error(
                            CompileErrors.DAO_CONSTRUCTOR_PARAM_TYPE,
                            param
                    );
                }
            }
        }
        return new ConstructorConf(isSuper, isEmpty);
    }

    private MethodSpec createConstructor(ParameterSpec param,
                                         List<AutoMethodPair> autoMethodPairs,
                                         List<SQLMethodPair> sqlMethodPairs,
                                         ConstructorConf conf,
                                         Configurations configurations) {
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
        builder.addStatement("this.$N = $N", sDatabaseField, param);
        Set<Pair<FieldSpec, TypeSpec>> set = new HashSet<>();
        boolean h2Mode = isH2Mode(configurations);

        autoMethodPairs.stream()
                .filter(autoMethodPair -> !autoMethodPair.fields.isEmpty())
                .forEach(autoMethodPair -> {
                    AtomicReference<Pair<FieldSpec, TypeSpec>> pair = new AtomicReference<>();
                    autoMethodPair.fields
                            .values()
                            .stream()
                            .filter(specPair ->
                                    specPair.first != null && specPair.second != null)
                            .forEach(pair::set);
                    set.add(pair.get());// deduplication
                });
        set.forEach(pair -> builder.addStatement("this.$N = $L",
                pair.first, pair.second));

        sqlMethodPairs.forEach(pair -> {
            String sql = processSqlIfH2Mode(
                    pair.sqlCustomMethod.getSql(), h2Mode);
            builder.addStatement("this.$N = new $T($L, $S)",
                    pair.fieldSpec,
                    JavaPoetClass.SQL_HANDLER,
                    sDatabaseField.name,
                    sql
            );
        });
        return builder.build();
    }

    private boolean isH2Mode(Configurations configurations) {
        if (configurations == null) {
            return false;
        }
        Configurations.Configuration configuration =
                configurations.findConfiguration(LightConfiguration.KEY_H2_DATABASE);
        if (configuration == null) {
            return false;
        }
        return configuration.toBoolean();
    }

    private String processSqlIfH2Mode(String sql, boolean h2Mode) {
        if (h2Mode) {
            return new SQLExpressionParser(sql).toUppercase();
        }
        return sql;
    }

    private List<AutoMethodPair> createAutoDeleteMethods() {
        List<AutoMethodPair> pairList = new ArrayList<>();
        mDao.getDeleteMethods().forEach(method -> {
            if (method.getSql() != null) {
                return;
            }
            final Map<String, Pair<FieldSpec, TypeSpec>> fields = new HashMap<>();
            method.getEntities().forEach((s, paramEntity) -> {
                fields.put(s,
                        Pair.createPair(
                                getOrCreateField(new DeleteUpdateMethodField("delete", paramEntity, null)),
                                new DeleteHandlerWriter(paramEntity).createAnonymous(this, sDatabaseField.name)));
            });

            MethodSpec methodImpl = MethodSpec.overriding(method.getElement())
                    .addModifiers(Modifier.FINAL)
                    .addCode(createAnnotatedMethodBody(method, fields))
                    .build();
            pairList.add(new AutoMethodPair(fields, methodImpl));
        });

        return pairList;
    }

    private List<AutoMethodPair> createUpdateMethods() {
        List<AutoMethodPair> pairList = new ArrayList<>();
        mDao.getUpdateMethods().forEach(method -> {
            final Map<String, Pair<FieldSpec, TypeSpec>> fields = new HashMap<>();
            method.getEntities().forEach((s, paramEntity) -> {
                fields.put(s,
                        Pair.createPair(getOrCreateField(
                                        new DeleteUpdateMethodField("update", paramEntity, null)),
                                new UpdateHandlerWriter(paramEntity, method).createAnonymous(this, sDatabaseField.name)));
            });
            MethodSpec methodImpl = MethodSpec.overriding(method.getElement())
                    .addModifiers(Modifier.FINAL)
                    .addCode(createAnnotatedMethodBody(method, fields))
                    .build();
            pairList.add(new AutoMethodPair(fields, methodImpl));
        });

        return pairList;
    }

    private List<AutoMethodPair> createInsertMethods() {
        List<AutoMethodPair> pairList = new ArrayList<>();
        mDao.getInsertMethods().forEach(method -> {
            final Map<String, Pair<FieldSpec, TypeSpec>> fields = new HashMap<>();
            method.getEntities().forEach((s, paramEntity) -> {
                fields.put(s,
                        Pair.createPair(getOrCreateField(new InsertMethodField(paramEntity, method.getOnConflict())),
                                new InsertHandlerWriter(method, paramEntity).createAnonymous(this, sDatabaseField.name)));
            });
            MethodSpec methodImpl = MethodSpec.overriding(method.getElement())
                    .addModifiers(Modifier.FINAL)
                    .addCode(createAnnotatedMethodBody(method, fields))
                    .build();
            pairList.add(new AutoMethodPair(fields, methodImpl));
        });
        return pairList;
    }

    @SuppressWarnings("unchecked")
    private CodeBlock createAnnotatedMethodBody(AnnotatedMethod<? extends Parameter> method,
                                                Map<String, Pair<FieldSpec, TypeSpec>> fields) {
        if (fields.isEmpty()) {
            return CodeBlock.builder().build();
        }

        GenerateCodeBlock block = new GenerateCodeBlock(this);
        method.getBinder()
                .writeBlock(
                        (List<Parameter>) method.getParameters(),
                        fields,
                        block
                );
        return block.generate();
    }

    private List<SQLMethodPair> createQueryMethods() {
        List<SQLMethodPair> pairList = new ArrayList<>();
        mDao.getQueryMethods().forEach(method -> {
            FieldSpec fieldSpec = getOrCreateField(new QueryHandlerField(method));
            MethodSpec methodImpl = MethodSpec.overriding(method.getElement())
                    .addModifiers(Modifier.FINAL)
                    .addCode(createQueryMethodBody(method, fieldSpec))
                    .build();
            pairList.add(new SQLMethodPair(method, methodImpl, fieldSpec));
        });

        return pairList;
    }

    private List<SQLMethodPair> createCustomDeleteMethods() {
        List<SQLMethodPair> pairList = new ArrayList<>();
        mDao.getDeleteMethods().forEach(method -> {
            if (method.getSql() == null) {
                return;
            }
            FieldSpec fieldSpec =
                    getOrCreateField(new CustomDeleteMethodField(method));
            MethodSpec methodImpl = MethodSpec.overriding(method.getElement())
                    .addModifiers(Modifier.FINAL)
                    .addCode(createQueryMethodBody(method, fieldSpec))
                    .build();
            pairList.add(new SQLMethodPair(method, methodImpl, fieldSpec));
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

    private CodeBlock createQueryMethodBody(SQLCustomMethod method, FieldSpec field) {
        SQLCustomMethodWriter writer = new SQLCustomMethodWriter(method);
        GenerateCodeBlock block = new GenerateCodeBlock(this);
        final String stmtVar = block.getTempVar("_stmt");
        writer.prepare(stmtVar, field.name, block);

        method.getResultBinder()
                .writeBlock(field.name, stmtVar, true,
                        !TypeUtil.isVoid(method.getReturnType()),
                        method.isTransaction(), block);
        return block.generate();
    }

    private static String identifierParamNameAndType(List<SQLCustomParameter> parameters) {
        StringBuilder builder = new StringBuilder();
        parameters.forEach(parameter -> {
            builder.append("__")
                    .append(StringUtil.firstUpperCase(parameter.getName()))
                    .append("_")
                    .append(typeMirrorToFieldName(parameter.getTypeMirror()));
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
            return ((ClassName) typeName).canonicalName()
                    .replace('.', '_');
        }
        return typeName.toString().replace('.', '_');
    }

    private static String typeMirrorToFieldName(TypeMirror typeMirror) {
        // replace all invalid symbols
        return typeMirror.toString()
                .replace('.', '_')
                .replace("<", "Of")
                .replace(">", "")
                .replace("[]", "Array")
                .replace('?', '$')
                .replace(",", "And")
                .replace(' ', '_');
    }

    private static class QueryHandlerField extends SharedFieldSpec {
        private final String sql;

        private QueryHandlerField(QueryMethod method) {
            super("queryHandlerOf" + StringUtil
                            .firstUpperCase(method.getElement().getSimpleName().toString())
                            + identifierParamNameAndType(method.getParameters()),
                    JavaPoetClass.SQL_HANDLER);
            this.sql = method.getSql();
        }

        @Override
        String getUniqueKey() {
            return "QueryHandler" + baseName + sql;
        }

        @Override
        void prepare(ClassWriter writer, FieldSpec.Builder builder) {
            builder.addModifiers(Modifier.PRIVATE, Modifier.FINAL);
        }
    }

    private static class CustomDeleteMethodField extends SharedFieldSpec {
        private final String sql;

        private CustomDeleteMethodField(DeleteMethod method) {
            super("customDeleteHandlerOf" +
                            StringUtil.firstUpperCase(
                                    method.getElement().getSimpleName().toString()) +
                            identifierParamNameAndType(method.getParameters()),
                    JavaPoetClass.SQL_HANDLER);
            this.sql = method.getSql();
        }

        @Override
        String getUniqueKey() {
            return "CustomDeleteHandler" + baseName + sql;
        }

        @Override
        void prepare(ClassWriter writer, FieldSpec.Builder builder) {
            builder.addModifiers(Modifier.PRIVATE, Modifier.FINAL);
        }
    }

    private static class DeleteUpdateMethodField extends SharedFieldSpec {
        private final ParamEntity entity;
        private final String prefix;
        private final String onConflict;

        DeleteUpdateMethodField(String prefix, ParamEntity entity, OnConflictStrategy onConflictStrategy) {
            super(prefix + "_deleteUpdateHandlerOf" + entityFieldName(entity),
                    ParameterizedTypeName.get(JavaPoetClass.DELETE_UPDATE_HANDLER,
                            entity.getPojo().getTypeName()));
            this.prefix = prefix;
            this.entity = entity;
            onConflict = onConflictStrategy == null ?
                    "" : onConflictStrategy.name();
        }

        @Override
        String getUniqueKey() {
            return prefix + "DeleteUpdateHandler-" +
                    entity.getPojo().getTypeName() +
                    entity.getTableName() +
                    onConflict;
        }

        @Override
        void prepare(ClassWriter writer, FieldSpec.Builder builder) {
            builder.addModifiers(Modifier.FINAL, Modifier.PRIVATE);
        }
    }


    private static class InsertMethodField extends SharedFieldSpec {
        private final ParamEntity entity;
        private final OnConflictStrategy onConflictStrategy;

        InsertMethodField(ParamEntity entity, OnConflictStrategy onConflictStrategy) {
            super("insertHandlerOf" + entityFieldName(entity),
                    ParameterizedTypeName.get(JavaPoetClass.INSERT_HANDLER,
                            entity.getPojo().getTypeName()));
            this.entity = entity;
            this.onConflictStrategy = onConflictStrategy;
        }

        @Override
        String getUniqueKey() {
            return "InsertHandler-" + entity.getPojo().getTypeName() +
                    entity.getTableName() +
                    onConflictStrategy.name();
        }

        @Override
        void prepare(ClassWriter writer, FieldSpec.Builder builder) {
            builder.addModifiers(Modifier.FINAL, Modifier.PRIVATE);
        }
    }

    private static class AutoMethodPair {
        final Map<String, Pair<FieldSpec, TypeSpec>> fields;
        final MethodSpec methodImpl;

        public AutoMethodPair(Map<String, Pair<FieldSpec, TypeSpec>> fields,
                              MethodSpec methodImpl) {
            this.fields = fields;
            this.methodImpl = methodImpl;
        }
    }

    private static class SQLMethodPair {
        final SQLCustomMethod sqlCustomMethod;
        final MethodSpec methodSpec;
        final FieldSpec fieldSpec;

        private SQLMethodPair(SQLCustomMethod sqlCustomMethod,
                              MethodSpec methodSpec,
                              FieldSpec fieldSpec) {
            this.methodSpec = methodSpec;
            this.sqlCustomMethod = sqlCustomMethod;
            this.fieldSpec = fieldSpec;
        }
    }

}
