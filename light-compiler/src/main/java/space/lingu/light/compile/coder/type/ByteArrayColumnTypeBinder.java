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

import space.lingu.light.SQLDataType;
import space.lingu.light.compile.coder.ColumnTypeBinder;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.javac.ProcessEnv;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author RollW
 */
public class ByteArrayColumnTypeBinder extends ColumnTypeBinder {
    public ByteArrayColumnTypeBinder(TypeMirror type) {
        super(type, SQLDataType.BINARY);
    }

    @Override
    public void readFromResultSet(String outVarName,
                                  String resultSetName,
                                  String indexName,
                                  GenerateCodeBlock block) {
        readValueWithCheckIndex(outVarName, resultSetName,
                indexName,
                "getBytes",
                "null", block);
    }

    @Override
    public void bindToStatement(String stmtVarName,
                                String indexVarName,
                                String valueVarName,
                                GenerateCodeBlock block) {
        bindToStatementWithNullable(stmtVarName,
                indexVarName, valueVarName,
                "setBytes", block);
    }

    public static ByteArrayColumnTypeBinder create(ProcessEnv env) {
        TypeMirror byteType = env.getTypeUtils().getPrimitiveType(TypeKind.BYTE);
        return new ByteArrayColumnTypeBinder(
                env.getTypeUtils().getArrayType(byteType));
    }

}
