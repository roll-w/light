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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import space.lingu.light.compile.MethodNames;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.javac.ElementUtils;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.Configurable;
import space.lingu.light.compile.struct.Database;
import space.lingu.light.compile.struct.DatabaseDaoMethod;

import javax.lang.model.element.Modifier;

/**
 * Write to {@code Database_Impl.java} file.
 *
 * @author RollW
 */
public class DatabaseWriter extends ClassWriter {
    private final Database mDatabase;

    public DatabaseWriter(Database database, ProcessEnv env) {
        super(database.getImplClassName(), database.getSuperClassName(), env);
        mDatabase = database;
    }

    @Override
    protected TypeSpec.Builder createTypeSpecBuilder() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(implClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(createClearAllTablesMethod())
                .addMethod(createRegisterTablesMethod())
                .superclass(ClassName.get(mDatabase.getSuperClassElement()));
        writeDaos();
        addDaoImpl(builder);
        return builder;
    }

    private MethodSpec createRegisterTablesMethod() {
        return MethodSpec.methodBuilder(MethodNames.sRegisterAllTables)
                .addComment("here creates all runtime structures")
                .addCode(createRegisterRuntimeStructCode())
                .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(TypeName.VOID)
                .build();
    }

    private CodeBlock createRegisterRuntimeStructCode() {
        GenerateCodeBlock block = new GenerateCodeBlock(this);
        String dbConfVarName = writeDatabaseConf(block);
        mDatabase.getDataTableList().forEach(dataTable -> {
            String qualifiedName = dataTable.getTypeCompileType().getQualifiedName().toString();
            block.builder().add("\n// start create " + qualifiedName + " structure. \n");
            RuntimeStructWriter writer = new RuntimeStructWriter(dataTable);
            final String tableVar = writer.writeDataTable(block, dbConfVarName);
            block.builder().addStatement("this.$L($L)",
                            MethodNames.sRegisterTable, tableVar)
                    .add("// end create " + qualifiedName + " structure.\n");
        });
        return block.builder().build();
    }

    private String writeDatabaseConf(GenerateCodeBlock block) {
        return Configurable.writeConfiguration(mDatabase, "Db", block);
    }


    private void writeDaos() {
        // write all daos

        mDatabase.getDatabaseDaoMethods().forEach(method -> {
            DaoWriter writer = new DaoWriter(method.getDao(),
                    mDatabase.getSuperClassElement(), mEnv);
            try {
                writer.write();
            } catch (FilterWriteException ignored) {
            }
        });
    }

    private void addDaoImpl(TypeSpec.Builder builder) {
        GenerateCodeBlock block = new GenerateCodeBlock(this);
        mDatabase.getDatabaseDaoMethods().forEach(method -> {
            String name = method.getDao().getSimpleName();
            String fieldName = block.getTempVar("_" + name);
            FieldSpec field = FieldSpec.builder(
                    method.getDao().getTypeCompileType().toTypeName(),
                    fieldName, Modifier.PRIVATE, Modifier.VOLATILE).build();
            builder.addField(field).addMethod(createDaoGetter(field, method));
        });
    }

    private MethodSpec createDaoGetter(FieldSpec field, DatabaseDaoMethod method) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getElement().getSimpleName().toString())
                .addAnnotation(Override.class)
                .returns(method.getDao().getTypeCompileType().toTypeName());
        if (ElementUtils.isPublic(method.getElement())) {
            methodBuilder.addModifiers(Modifier.PUBLIC);
        } else if (ElementUtils.isProtected(method.getElement())) {
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
        MethodSpec.Builder builder = MethodSpec.methodBuilder("clearAllTables")
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
