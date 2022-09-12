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
import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.Configurable;
import space.lingu.light.compile.struct.Database;
import space.lingu.light.compile.struct.DatabaseDaoMethod;

import javax.lang.model.element.Modifier;

/**
 * 写入{@code Database_Impl}类中
 * @author RollW
 */
public class DatabaseWriter extends ClassWriter {
    private final Database mDatabase;

    public DatabaseWriter(Database database, ProcessEnv env) {
        super(database.getImplClassName(), env);
        mDatabase = database;
    }

    @Override
    protected TypeSpec.Builder createTypeSpecBuilder() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addInitializerBlock(createRegisterRuntimeStructCode())
                .addMethod(createClearAllTablesMethod())
                .superclass(ClassName.get(mDatabase.getSuperClassElement()));
        writeDaos();
        addDaoImpl(builder);
        return builder;
    }

    private CodeBlock createRegisterRuntimeStructCode() {
        GenerateCodeBlock block = new GenerateCodeBlock(this);
        String dbConfVarName = writeDatabaseConf(block);
        mDatabase.getDataTableList().forEach(dataTable -> {
            RuntimeStructWriter writer = new RuntimeStructWriter(dataTable);
            final String tableVar = writer.writeDataTable(block, dbConfVarName);
            block.builder().addStatement("this.registerTable($L)", tableVar);
        });
        return block.builder().build();
    }

    private String writeDatabaseConf(GenerateCodeBlock block) {
        return Configurable.writeConfiguration(mDatabase,"Db", block);
    }


    private void writeDaos() {
        // 生成所有的Dao
        mDatabase.getDatabaseDaoMethods().forEach(method -> {
            DaoWriter writer = new DaoWriter(method.getDao(),
                    mDatabase.getSuperClassElement(), mEnv);
            writer.write();
        });
    }

    private void addDaoImpl(TypeSpec.Builder builder) {
        GenerateCodeBlock block = new GenerateCodeBlock(this);
        mDatabase.getDatabaseDaoMethods().forEach(method -> {
            String name = method.getDao().getSimpleName();
            String fieldName = block.getTempVar("_" + name);
            FieldSpec field = FieldSpec.builder(ClassName.get(method.getDao().getElement()),
                            fieldName, Modifier.PRIVATE, Modifier.VOLATILE).build();
            builder.addField(field).addMethod(createDaoGetter(field, method));
        });
    }

    private MethodSpec createDaoGetter(FieldSpec field, DatabaseDaoMethod method) {
        if (!method.getElement().getParameters().isEmpty()) {
            throw new LightCompileException("A Dao getter method must be a parameterless method.");
        }

        MethodSpec.Builder methodBuilder =  MethodSpec.methodBuilder(method.getElement().getSimpleName().toString())
                .addAnnotation(Override.class)
                .returns(ClassName.get(method.getDao().getElement()));
        if (ElementUtil.isPublic(method.getElement())) {
            methodBuilder.addModifiers(Modifier.PUBLIC);
        } else if (ElementUtil.isProtected(method.getElement())) {
            methodBuilder.addModifiers(Modifier.PROTECTED);
        }

        methodBuilder.beginControlFlow("if ($N != null)", field)
                .addStatement("return $N", field)
                .nextControlFlow("else")
                .beginControlFlow("synchronized(this)")
                .beginControlFlow("if ($N == null)", field)
                .addStatement("$N = new $T(this)", field, method.getDao().getImplClassName())
                .endControlFlow()
                .addStatement("return $N", field)
                .endControlFlow()
                .endControlFlow();
        return methodBuilder.build();
    }

    private MethodSpec createClearAllTablesMethod() {
        MethodSpec.Builder builder =  MethodSpec.methodBuilder("clearAllTables")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .returns(TypeName.VOID);
        mDatabase.getDataTableList().forEach(dataTable -> {
            builder.addStatement("this.destroyTable($S)", dataTable.getTableName());
        });

        return builder.build();
    }

    static class IntShareFieldSpec extends SharedFieldSpec {

        IntShareFieldSpec(String baseName) {
            super(baseName, ClassName.INT);
        }

        @Override
        String getUniqueKey() {
            return baseName;
        }

        @Override
        void prepare(ClassWriter writer, FieldSpec.Builder builder) {
            builder.addModifiers(Modifier.PUBLIC);
        }
    }
}
