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
import space.lingu.light.compile.struct.UpdateMethod;

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
    private final String mTableName;
    private final Pojo mPojo;
    private final UpdateMethod mMethod;

    public UpdateHandlerWriter(ParamEntity entity, UpdateMethod updateMethod) {
        mEntity = entity;
        mPojo = entity.getPojo();
        mTableName = entity.getTableName();
        mMethod = updateMethod;
    }

    public TypeSpec createAnonymous(ClassWriter writer, String dbParam) {
        StringJoiner keys = new StringJoiner(", ");
        StringJoiner params = new StringJoiner(", ");
        mEntity.getPrimaryKey().getFields().getFields().forEach(field ->
                keys.add("\"" + field.getColumnName() + "\""));

        mEntity.getPojo().getFields().getFields().forEach(field ->
                params.add("\"" + field.getColumnName() + "\""));

        GenerateCodeBlock queryBlock = new GenerateCodeBlock(writer);
        ArrayTypeName stringArray =
                ArrayTypeName.of(JavaPoetClass.LangNames.STRING);
        String primaryKeysVar = queryBlock.getTempVar("_pKeys");
        String paramsVar = queryBlock.getTempVar("_values");

        FieldSpec primaryKeysField = FieldSpec.builder(stringArray,
                        primaryKeysVar, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new String[]{$L}", keys.toString())
                .build();
        FieldSpec paramsField = FieldSpec.builder(stringArray,
                        paramsVar, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addModifiers()
                .initializer("new String[]{$L}", params.toString())
                .build();

        queryBlock.builder()
                .addStatement("return $N.getDialectProvider().getGenerator().update($S, $T.$L, $L, $L)",
                        DaoWriter.sDatabaseField, mTableName,
                        JavaPoetClass.ON_CONFLICT_STRATEGY,
                        mMethod.getOnConflict(),
                        primaryKeysVar, paramsVar);

        TypeSpec.Builder builder = TypeSpec.anonymousClassBuilder("$L", dbParam)
                .superclass(ParameterizedTypeName.get(JavaPoetClass.DELETE_UPDATE_HANDLER, mPojo.getTypeName()))
                .addField(primaryKeysField)
                .addField(paramsField)
                .addMethod(
                        MethodSpec.methodBuilder("createQuery")
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(Override.class)
                                .returns(JavaPoetClass.LangNames.STRING)
                                .addCode(queryBlock.generate())
                                .build()
                );

        GenerateCodeBlock bindBlock = new GenerateCodeBlock(writer);
        MethodSpec.Builder bindMethodBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                .addAnnotation(Override.class)
                .addParameter(ParameterSpec.builder(JavaPoetClass.JdbcNames.PREPARED_STMT, "stmt")
                        .build())
                .addParameter(ParameterSpec
                        .builder(mPojo.getTypeName(), "value")
                        .build())
                .returns(TypeName.VOID);

        List<FieldReadWriteWriter.FieldWithNumber> fieldWithNumberList = new ArrayList<>();
        IntStream.range(0, mPojo.getFields().getFields().size()).forEach(value -> {
            Field field = mPojo.getFields().getFields().get(value);
            fieldWithNumberList.add(new FieldReadWriteWriter.FieldWithNumber(
                    field,
                    String.valueOf(value + 1))
            );
        });

        final int primaryKeyStart = mPojo.getFields().getFields().size();
        IntStream.range(0, mEntity.getPrimaryKey().getFields().getFields().size()).forEach(value -> {
            Field field = mEntity.getPrimaryKey().getFields().getFields().get(value);
            fieldWithNumberList.add(new FieldReadWriteWriter.FieldWithNumber(
                    field,
                    String.valueOf(value + 1 + primaryKeyStart))
            );
        });

        FieldReadWriteWriter.bindToStatement("value", "stmt", fieldWithNumberList, bindBlock);
        bindMethodBuilder.addCode(bindBlock.builder().build());

        builder.addMethod(bindMethodBuilder.build());
        return builder.build();
    }
}
