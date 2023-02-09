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

package space.lingu.light.compile.coder;

import com.squareup.javapoet.TypeName;
import space.lingu.light.LightRuntimeException;
import space.lingu.light.SQLDataType;
import space.lingu.light.compile.javac.TypeCompileType;

import java.sql.SQLException;
import java.sql.Types;

/**
 * value binder
 *
 * @author RollW
 */
@SuppressWarnings("all")
public abstract class ColumnTypeBinder implements StatementBinder, ColumnValueReader {
    protected final TypeCompileType type;
    protected final SQLDataType dataType;
    protected final TypeName typeName;

    public ColumnTypeBinder(TypeCompileType type,
                            SQLDataType dataType) {
        this.type = type;
        this.dataType = dataType;
        typeName = type.toTypeName();
    }

    @Override
    public TypeCompileType type() {
        return type;
    }

    @Override
    public SQLDataType getDataType() {
        return dataType;
    }

    protected void readValueWithCheckIndex(String outVarName, String resultSetName,
                                           String indexName, String methodName,
                                           String defaultValue,
                                           GenerateCodeBlock block) {
        block.builder()
                .beginControlFlow("if ($L < 0)", indexName)
                .addStatement("$L = $L", outVarName, defaultValue)
                .nextControlFlow("else")
                .addStatement("$L = $L.$L($L)",
                        outVarName, resultSetName, methodName, indexName)
                .endControlFlow();
    }

    protected void bindToStatementWithNullable(String stmtVarName, String indexVarName,
                                               String valueVarName, String methodName, GenerateCodeBlock block) {
        block.builder()
                .beginControlFlow("try")
                .beginControlFlow("if ($L == null)", valueVarName)
                .addStatement("$L.setNull($L, $L)", stmtVarName, indexVarName, Types.NULL)
                .nextControlFlow("else")
                .addStatement("$L.$L($L, $L)", stmtVarName, methodName, indexVarName, valueVarName)
                .endControlFlow()
                .nextControlFlow("catch ($T e)", SQLException.class)
                .addStatement("throw new $T(e)", LightRuntimeException.class)
                .endControlFlow();
    }
}
