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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import space.lingu.light.LightRuntimeException;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.query.row.RowConverter;

import javax.lang.model.type.TypeMirror;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author RollW
 */
public class ListQueryResultConverter extends QueryResultConverter {
    private final TypeMirror mType;
    private final RowConverter mConverter;

    public ListQueryResultConverter(TypeMirror type, RowConverter converter) {
        super(Collections.singletonList(converter));
        mConverter = converter;
        mType = type;
    }

    @Override
    public void convert(String outVarName, String resultSetName, GenerateCodeBlock block) {
        mConverter.onResultSetReady(resultSetName, block);
        TypeName listType = ParameterizedTypeName
                .get(ClassName.get(List.class), ClassName.get(mType));
        TypeName arrayListType = ParameterizedTypeName
                .get(ClassName.get(ArrayList.class), ClassName.get(mType));
        final String tempVar = block.getTempVar("_item");
        block.builder().addStatement("final $T $L = new $T()", listType, outVarName, arrayListType)
                .beginControlFlow("try")
                .beginControlFlow("while ($L.next())", resultSetName)
                .addStatement("final $T $L", ClassName.get(mType), tempVar);

        mConverter.convert(tempVar, resultSetName, block);
        block.builder().addStatement("$L.add($L)", outVarName, tempVar)
                .endControlFlow()
                .nextControlFlow("catch ($T e)", SQLException.class)
                .addStatement("throw new $T(e)", LightRuntimeException.class)
                .endControlFlow();
    }
}
