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
import space.lingu.light.compile.JavaPoetClass;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.struct.Field;
import space.lingu.light.compile.struct.ParamEntity;
import space.lingu.light.compile.struct.Pojo;
import space.lingu.light.util.Pair;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.IntStream;

/**
 * @author RollW
 */
public class UpdateHandlerWriter {
    private final ParamEntity mEntity;
    private final String tableName;
    private final Pojo mPojo;

    public UpdateHandlerWriter(ParamEntity entity) {
        mEntity = entity;
        mPojo = entity.getPojo();
        tableName = entity.getTableName();
    }

    public TypeSpec createAnonymous(ClassWriter writer, String dbParam) {
        StringJoiner args = new StringJoiner(", ");
        mEntity.getPrimaryKey().getFields().fields.forEach(field -> {
            args.add("\"" + field.getColumnName() + "\"");
        });
        GenerateCodeBlock bindBlock = new GenerateCodeBlock(writer);
        TypeSpec.Builder builder = TypeSpec.anonymousClassBuilder("$L", dbParam)
                .superclass(ParameterizedTypeName.get(JavaPoetClass.DELETE_UPDATE_HANDLER, mPojo.getTypeName()))
                .addMethod(
                        MethodSpec.methodBuilder("createQuery")
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(Override.class)
                                .returns(ClassName.get("java.lang", "String"))
                                .addStatement("return $N.getDialectProvider().getGenerator().generateUpdate($S, $L)",
                                        DaoWriter.sDatabaseField, tableName, args.toString())
                                .build()
                );

        MethodSpec.Builder bindMethodBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                .addAnnotation(Override.class)
                .addParameter(ParameterSpec.builder(JavaPoetClass.JdbcNames.PREPARED_STMT, "stmt")
                        .build())
                .addParameter(ParameterSpec
                        .builder(mPojo.getTypeName(), "value")
                        .build())
                .returns(TypeName.VOID);

        List<Pair<Field, String>> fieldWithNumber = new ArrayList<>();
        IntStream.range(0, mPojo.getFields().size()).forEach(value -> {
            Field field = mPojo.getFields().get(value);
            fieldWithNumber.add(Pair.createPair(field, String.valueOf(value + 1)));
        });

        final int primaryKeyStart = mPojo.getFields().size();
        IntStream.range(0, mEntity.getPrimaryKey().getFields().fields.size()).forEach(value -> {
            Field field = mEntity.getPrimaryKey().getFields().fields.get(value);
            fieldWithNumber.add(Pair.createPair(field,
                    String.valueOf(value + 1 + primaryKeyStart)));
        });

        FieldReadWriteWriter.bindToStatement("value", "stmt", fieldWithNumber, bindBlock);
        bindMethodBuilder.addCode(bindBlock.builder().build());

        builder.addMethod(bindMethodBuilder.build());
        return builder.build();
    }
}
