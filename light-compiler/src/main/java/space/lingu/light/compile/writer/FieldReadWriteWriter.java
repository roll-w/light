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

import com.squareup.javapoet.TypeName;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.struct.Constructor;
import space.lingu.light.compile.struct.Field;
import space.lingu.light.compile.struct.Pojo;
import space.lingu.light.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 实体类读写
 *
 * @author RollW
 */
public class FieldReadWriteWriter {
    private final Field field;
    private final String indexVar;

    public FieldReadWriteWriter(Field field, String index) {
        this.field = field;
        this.indexVar = index;
    }

    public static void bindToStatement(String owner, String stmt,
                                       List<FieldWithNumber> fieldsWithIndex,
                                       GenerateCodeBlock block) {
        fieldsWithIndex.forEach(fieldWithNumber -> {
            // TODO: embedded field
            new FieldReadWriteWriter(fieldWithNumber.field, fieldWithNumber.indexNum)
                    .bindToStatement(owner, stmt, block);
        });
    }

    // TODO: refactor this method
    public static void readFromResultSet(String owner, Pojo outPojo, String resSetVar,
                                         List<FieldWithNumber> fieldsWithIndex,
                                         GenerateCodeBlock block) {
        Map<String, FieldWithNumber> constructorField = new HashMap<>();
        List<FieldWithNumber> filteredFields = fieldsWithIndex
                .stream()
                .filter(fieldWithNumber ->
                        fieldWithNumber.field.getSetter().getCallType() == Field.CallType.CONSTRUCTOR)
                .collect(Collectors.toList());
        filteredFields.forEach(fieldWithNumber -> constructorField.put(
                new FieldReadWriteWriter(fieldWithNumber.field, fieldWithNumber.indexNum).readIntoTempVar(resSetVar,
                        fieldWithNumber.field
                                .getSetter()
                                .getVariableCompileType()
                                .getType()
                                .toTypeName()
                        , block),
                fieldWithNumber)
        );
        setFromConstructor(owner, outPojo.getConstructor(), outPojo.getTypeName(), constructorField, block);
        fieldsWithIndex.forEach(pair ->
                new FieldReadWriteWriter(pair.field, pair.indexNum)
                        .readFromResultSet(owner, resSetVar, block)
        );
    }

    public static void setFromConstructor(String outVar, Constructor constructor,
                                          TypeName typeName,
                                          Map<String, FieldWithNumber> varNames,
                                          GenerateCodeBlock block) {
        if (constructor == null) {
            block.builder().addStatement("$L = new $T()", outVar, typeName);
            return;
        }
        List<String> vars = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();
        constructor.getFields().forEach(constructorField ->
                varNames.forEach((tempVarName, fieldWithNumber) -> {
                    String name = fieldWithNumber.field.getName();
                    if (usedNames.contains(name)) {
                        return;
                    }
                    if (constructorField.getPossibleCandidateName().contains(name)) {
                        vars.add(tempVarName);
                        usedNames.add(name);
                    }
                }));
        StringJoiner args = new StringJoiner(", ");
        vars.forEach(args::add);
        constructor.writeConstructor(outVar, args.toString(), block.builder());
    }

    private void bindToStatement(String owner, String stmt, GenerateCodeBlock block) {
        String varName;
        if (field.getGetter().getCallType() == Field.CallType.FIELD) {
            varName = owner + "." + field.getName();
        } else {
            varName = owner + "." + field.getGetter().getName() + "()";
        }
        field.getStatementBinder().bindToStatement(stmt, indexVar, varName, block);
    }

    private void readFromResultSet(String owner, String resSetVar,
                                   GenerateCodeBlock block) {
        switch (field.getSetter().getCallType()) {
            case FIELD: {
                field.getColumnValueReader().readFromResultSet(
                        owner + "." + field.getSetter().getName(),
                        resSetVar, indexVar, block);
                break;
            }
            case METHOD: {
                String tempVar = block.getTempVar("_tmp" + StringUtil.firstUpperCase(field.getName()));
                block.builder().addStatement("final $T $L",
                        field.getSetter()
                                .getVariableCompileType().getType()
                                .toTypeName(),
                        tempVar);
                field.getColumnValueReader().readFromResultSet(tempVar, resSetVar, indexVar, block);
                block.builder().addStatement("$L.$L($L)", owner, field.getSetter().getName(), tempVar);
                break;
            }
            case CONSTRUCTOR: {
                // leave it alone.
                break;
            }
        }
    }

    private String readIntoTempVar(String resSetName, TypeName typeName, GenerateCodeBlock block) {
        final String tmpVar = block.getTempVar("_tmp" + StringUtil.firstUpperCase(field.getName()));
        block.builder().addStatement("final $T $L", typeName, tmpVar);
        field.getColumnValueReader().readFromResultSet(tmpVar, resSetName, indexVar, block);
        return tmpVar;
    }

    public static class FieldWithNumber {
        final Field field;
        final String indexNum;

        public FieldWithNumber(Field field, String indexNum) {
            this.field = field;
            this.indexNum = indexNum;
        }
    }
}
