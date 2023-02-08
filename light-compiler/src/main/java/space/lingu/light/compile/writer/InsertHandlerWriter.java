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

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import space.lingu.light.OnConflictStrategy;
import space.lingu.light.compile.JavaPoetClass;
import space.lingu.light.compile.struct.Field;
import space.lingu.light.compile.struct.InsertMethod;
import space.lingu.light.compile.struct.ParamEntity;
import space.lingu.light.compile.struct.Pojo;

import javax.lang.model.element.Modifier;
import java.util.List;

/**
 * @author RollW
 */
public class InsertHandlerWriter {
    private final String tableName;
    private final Pojo pojo;
    private final OnConflictStrategy onConflictStrategy;

    public InsertHandlerWriter(String tableName, Pojo pojo, OnConflictStrategy onConflictStrategy) {
        this.tableName = tableName;
        this.pojo = pojo;
        this.onConflictStrategy = onConflictStrategy;
    }

    public InsertHandlerWriter(InsertMethod method, ParamEntity entity) {
        this(entity.getTableName(), entity.getPojo(), method.getOnConflict());
    }

    public TypeSpec createAnonymous(ClassWriter writer, String dbParam) {
        StringBuilder args = new StringBuilder();
        List<Field> fields = pojo.getFields().getFields();
        for (int i = 0; i < fields.size(); i++) {
            if (i < fields.size() - 1) {
                args.append("\"").append(fields.get(i).getColumnName()).append("\", ");
            } else {
                args.append("\"").append(fields.get(i).getColumnName()).append("\"");
            }
        }
        AnnotatedMethodWriter delegate = new AnnotatedMethodWriter(pojo);
        TypeSpec.Builder builder = TypeSpec.anonymousClassBuilder("$L", dbParam)
                .superclass(ParameterizedTypeName.get(JavaPoetClass.INSERT_HANDLER, pojo.getTypeName()))
                .addMethod(
                        MethodSpec.methodBuilder("createQuery")
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(Override.class)
                                .returns(JavaPoetClass.LangNames.STRING)
                                .addStatement("return $N.getDialectProvider().getGenerator().insert($S, $T.$L, $L)",
                                        DaoWriter.sDatabaseField,
                                        tableName,
                                        JavaPoetClass.ON_CONFLICT_STRATEGY,
                                        onConflictStrategy,
                                        args.toString())
                                .build());

        builder.addMethod(delegate.createBindMethod(writer, fields));
        return builder.build();
    }

}
