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
import space.lingu.light.SQLDataType;

import javax.lang.model.type.TypeMirror;

/**
 * value binder
 *
 * @author RollW
 */
@SuppressWarnings("all")
public abstract class ColumnTypeBinder implements StatementBinder, ColumnValueReader {
    protected final TypeMirror type;
    protected final SQLDataType dataType;
    protected final TypeName typeName;

    public ColumnTypeBinder(TypeMirror type, SQLDataType dataType) {
        this.type = type;
        this.dataType = dataType;
        typeName = type == null
                ? null
                : TypeName.get(type);
    }

    @Override
    public TypeMirror type() {
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
}
