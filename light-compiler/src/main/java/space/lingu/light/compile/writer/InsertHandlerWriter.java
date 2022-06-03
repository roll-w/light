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
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.JavaPoetClass;
import space.lingu.light.compile.struct.Field;
import space.lingu.light.compile.struct.Pojo;
import space.lingu.light.compile.struct.ParamEntity;
import space.lingu.light.util.Pair;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author RollW
 */
public class InsertHandlerWriter {
    private final String tableName;
    private final Pojo pojo;

    public InsertHandlerWriter(String tableName, Pojo pojo) {
        this.tableName = tableName;
        this.pojo = pojo;
    }

    public InsertHandlerWriter(ParamEntity entity) {
        this(entity.getTableName(), entity.getPojo());
    }

    public TypeSpec createAnonymous(ClassWriter writer, String dbParam) {
        StringBuilder args = new StringBuilder();
        for (int i = 0; i < pojo.getFields().size(); i++) {
            if (i < pojo.getFields().size() - 1) {
                args.append("\"").append(pojo.getFields().get(i).getColumnName()).append("\", ");
            } else {
                args.append("\"").append(pojo.getFields().get(i).getColumnName()).append("\"");
            }
        }
        AnnotatedMethodWriter delegate = new AnnotatedMethodWriter(pojo);
        TypeSpec.Builder builder =  TypeSpec.anonymousClassBuilder("$L", dbParam)
                .superclass(ParameterizedTypeName.get(JavaPoetClass.INSERT_HANDLER, pojo.getTypeName()))
                .addMethod(
                        MethodSpec.methodBuilder("createQuery")
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(Override.class)
                                .returns(ClassName.get("java.lang", "String"))
                                .addStatement("return $N.getDialectProvider().getGenerator().generateInsert($S, $L)",
                                        DaoWriter.DATABASE_FIELD, tableName, args.toString())
                                .build());

        builder.addMethod(delegate.createBindMethod(writer, pojo.getFields()));
        return builder.build();
    }

}
