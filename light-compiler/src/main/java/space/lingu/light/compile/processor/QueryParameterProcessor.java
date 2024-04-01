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

package space.lingu.light.compile.processor;

import space.lingu.light.compile.javac.CompileType;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.VariableCompileType;
import space.lingu.light.compile.struct.QueryParameter;

/**
 * @author RollW
 */
public class QueryParameterProcessor implements Processor<QueryParameter> {
    private final VariableCompileType variableCompileType;
    private final CompileType containing;
    private final ProcessEnv env;

    public QueryParameterProcessor(VariableCompileType variableCompileType,
                                   CompileType containing,
                                   ProcessEnv env) {
        this.variableCompileType = variableCompileType;
        this.containing = containing;
        this.env = env;
    }

    @Override
    public QueryParameter process() {
        return new QueryParameter(
                variableCompileType,
                variableCompileType.getName()
        );
    }
}
