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
import space.lingu.light.compile.coder.custom.QueryContext;
import space.lingu.light.compile.coder.custom.row.RowConverter;
import space.lingu.light.compile.javac.TypeCompileType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author RollW
 */
public class ListQueryResultConverter extends AbstractQueryResultConverter {
    private final TypeCompileType type;
    private final RowConverter converter;

    public ListQueryResultConverter(TypeCompileType type, RowConverter converter) {
        super(converter);
        this.converter = converter;
        this.type = type;
    }

    @Override
    public void convert(QueryContext queryContext, GenerateCodeBlock block) {
        converter.onResultSetReady(queryContext, block);

        TypeName listType = ParameterizedTypeName
                .get(ClassName.get(List.class), type.toTypeName());
        TypeName arrayListType = ParameterizedTypeName
                .get(ClassName.get(ArrayList.class), type.toTypeName());
        final String tempVar = block.getTempVar("_item");
        block.builder().addStatement("final $T $L = new $T()", listType,
                        queryContext.getOutVarName(), arrayListType)
                .beginControlFlow("try")
                .beginControlFlow("while ($L.next())", queryContext.getResultSetVarName())
                .addStatement("final $T $L", type.toTypeName(), tempVar);

        QueryContext scopeContext = queryContext.fork(tempVar);

        converter.convert(scopeContext, block);
        block.builder().addStatement("$L.add($L)", queryContext.getOutVarName(), tempVar)
                .endControlFlow()
                .nextControlFlow("catch ($T e)", SQLException.class)
                .addStatement("throw new $T(e)", LightRuntimeException.class)
                .endControlFlow();
    }
}
