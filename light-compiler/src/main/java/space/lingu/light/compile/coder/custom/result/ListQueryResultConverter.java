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

package space.lingu.light.compile.coder.custom.result;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import space.lingu.light.LightRuntimeException;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.custom.row.RowConverter;
import space.lingu.light.compile.javac.TypeCompileType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author RollW
 */
public class ListQueryResultConverter extends QueryResultConverter {
    private final TypeCompileType mType;
    private final RowConverter mConverter;

    public ListQueryResultConverter(TypeCompileType type, RowConverter converter) {
        super(Collections.singletonList(converter));
        mConverter = converter;
        mType = type;
    }

    @Override
    public void convert(String outVarName, String resultSetName, GenerateCodeBlock block) {
        mConverter.onResultSetReady(resultSetName, block);
        TypeName listType = ParameterizedTypeName
                .get(ClassName.get(List.class), mType.toTypeName());
        TypeName arrayListType = ParameterizedTypeName
                .get(ClassName.get(ArrayList.class), mType.toTypeName());
        final String tempVar = block.getTempVar("_item");
        block.builder().addStatement("final $T $L = new $T()", listType, outVarName, arrayListType)
                .beginControlFlow("try")
                .beginControlFlow("while ($L.next())", resultSetName)
                .addStatement("final $T $L", mType.toTypeName(), tempVar);

        mConverter.convert(tempVar, resultSetName, block);
        block.builder().addStatement("$L.add($L)", outVarName, tempVar)
                .endControlFlow()
                .nextControlFlow("catch ($T e)", SQLException.class)
                .addStatement("throw new $T(e)", LightRuntimeException.class)
                .endControlFlow();
    }
}
