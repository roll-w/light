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

package space.lingu.light.compile.coder.query.result;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import space.lingu.light.compile.JavaPoetClass;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.query.row.RowConverter;
import space.lingu.light.compile.javac.TypeUtil;

import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author RollW
 */
public class ArrayQueryResultConverter extends QueryResultConverter {
    public final RowConverter converter;
    private final TypeMirror type;

    public ArrayQueryResultConverter(RowConverter converter) {
        super(Collections.singletonList(converter));
        this.converter = converter;
        this.type = converter.getOutType();
    }

    @Override
    public void convert(String outVarName, String resultSetName, GenerateCodeBlock block) {
        converter.onResultSetReady(resultSetName, block);
        TypeName arrayType = ArrayTypeName.of(ClassName.get(type));
        if (TypeUtil.isArray(type)) {
            block.builder().addStatement("final $T $L = new $T[$T.getResultSetSize($L)][]",
                    arrayType, outVarName,
                    ClassName.get(TypeUtil.getArrayElementType(type)),
                    JavaPoetClass.UtilNames.RESULT_SET_UTIL, resultSetName);
        } else {
            block.builder().addStatement("final $T $L = new $T[$T.getResultSetSize($L)]",
                    arrayType, outVarName,
                    ClassName.get(TypeUtil.getArrayElementType(type)),
                    JavaPoetClass.UtilNames.RESULT_SET_UTIL, resultSetName);
        }

        final String tempVar = block.getTempVar("_item");
        final String indexVar = block.getTempVar("_index");

        block.builder().addStatement("$T $L = 0", TypeName.INT, indexVar)
                .beginControlFlow("while ($L.next())", resultSetName)
                .addStatement("final $T $L", ClassName.get(type), tempVar);
        converter.convert(tempVar, resultSetName, block);
        block.builder().addStatement("$L[$L] = $L", outVarName, indexVar, tempVar)
                .addStatement("$L++", indexVar)
                .endControlFlow();
        converter.onResultSetFinish(block);

    }
}
