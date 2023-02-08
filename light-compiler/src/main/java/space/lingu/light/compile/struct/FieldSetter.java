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

package space.lingu.light.compile.struct;

import com.squareup.javapoet.CodeBlock;
import space.lingu.light.compile.javac.VariableCompileType;

/**
 * @author RollW
 */
public class FieldSetter {
    private final VariableCompileType variableCompileType;
    private final Field.CallType callType;
    private final String name;

    public void write(String owner, String in, CodeBlock.Builder builder) {
        String statement = null;
        switch (callType) {
            case FIELD: {
                statement = "$L.$L = $L";
                break;
            }
            case METHOD: {
                statement = "$L.$L($L)";
                break;
            }
        }
        if (statement == null) return;

        builder.addStatement(statement, owner, name, in);
    }

    public FieldSetter(VariableCompileType variableCompileType, Field.CallType callType, String name) {
        this.variableCompileType = variableCompileType;
        this.callType = callType;
        this.name = name;
    }

    public VariableCompileType getVariableCompileType() {
        return variableCompileType;
    }

    public Field.CallType getCallType() {
        return callType;
    }

    public String getName() {
        return name;
    }
}
