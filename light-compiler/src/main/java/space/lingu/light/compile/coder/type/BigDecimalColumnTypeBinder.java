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

package space.lingu.light.compile.coder.type;

import space.lingu.light.LightRuntimeException;
import space.lingu.light.SQLDataType;
import space.lingu.light.compile.coder.ColumnTypeBinder;
import space.lingu.light.compile.coder.ColumnValueReader;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.StatementBinder;
import space.lingu.light.compile.javac.ProcessEnv;

import javax.lang.model.type.TypeMirror;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;

/**
 * {@link BigDecimal} type binder.
 *
 * @author RollW
 */
public class BigDecimalColumnTypeBinder extends ColumnTypeBinder implements StatementBinder, ColumnValueReader {
    public BigDecimalColumnTypeBinder(TypeMirror type) {
        super(type, SQLDataType.DECIMAL);
    }

    @Override
    public void readFromResultSet(String outVarName, String resultSetName,
                                  String indexName, GenerateCodeBlock block) {
        readValueWithCheckIndex(outVarName,
                resultSetName, indexName,
                "getBigDecimal", "null", block);
    }

    @Override
    public void bindToStatement(String stmtVarName, String indexVarName,
                                String valueVarName, GenerateCodeBlock block) {
        block.builder()
                .beginControlFlow("try")
                .beginControlFlow("if ($L == null)", valueVarName)
                .addStatement("$L.setNull($L, $L)", stmtVarName, indexVarName, Types.NULL)
                .nextControlFlow("else")
                .addStatement("$L.setBigDecimal($L, $L)", stmtVarName, indexVarName, valueVarName)
                .endControlFlow()
                .nextControlFlow("catch ($T e)", SQLException.class)
                .addStatement("throw new $T(e)", LightRuntimeException.class)
                .endControlFlow();
    }

    public static BigDecimalColumnTypeBinder create(ProcessEnv env) {
        return new BigDecimalColumnTypeBinder(
                env.getElementUtils()
                        .getTypeElement("java.math.BigDecimal")
                        .asType()
        );
    }
}
