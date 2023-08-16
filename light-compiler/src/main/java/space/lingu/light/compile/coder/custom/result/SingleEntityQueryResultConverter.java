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

import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.custom.QueryContext;
import space.lingu.light.compile.coder.custom.row.RowConverter;
import space.lingu.light.compile.javac.TypeCompileType;

/**
 * Returns an entity.
 *
 * @author RollW
 */
public class SingleEntityQueryResultConverter extends AbstractQueryResultConverter {
    private final RowConverter mConverter;

    public SingleEntityQueryResultConverter(RowConverter converter) {
        super(converter);
        mConverter = converter;
    }

    @Override
    public void convert(QueryContext queryContext, GenerateCodeBlock block) {
        mConverter.onResultSetReady(queryContext, block);

        block.builder()
                .addStatement("final $T $L",
                        mConverter.getOutType().toTypeName(),
                        queryContext.getOutVarName())
                .beginControlFlow("if ($L.next())",
                        queryContext.getResultSetVarName());

        mConverter.convert(queryContext, block);

        block.builder()
                .nextControlFlow("else")
                .addStatement("$L = $L", queryContext.getOutVarName(),
                        getDefaultValue(mConverter.getOutType()))
                .endControlFlow();

        mConverter.onResultSetFinish(block);
    }

    private String getDefaultValue(TypeCompileType typeCompileType) {
        switch (typeCompileType.getTypeMirror().getKind()) {
            case BOOLEAN:
                return "false";
            case LONG:
                return "0L";
            case FLOAT:
                return "0F";
            case DOUBLE:
                return "0.0";
            case CHAR:
            case INT:
            case BYTE:
            case SHORT:
                return "0";
            default: return null;
        }
    }
}
