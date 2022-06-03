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

package space.lingu.light.compile.coder.query.binder;

import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.StatementBinder;

import javax.lang.model.type.TypeMirror;

/**
 * @author RollW
 */
public class BasicQueryParameterBinder extends QueryParameterBinder {
    public final StatementBinder binder;

    public BasicQueryParameterBinder(StatementBinder binder) {
        super(false);
        this.binder = binder;
    }

    @Override
    public TypeMirror type() {
        return binder.type();
    }

    @Override
    public void bindToStatement(String stmtVarName, String indexVarName, String valueVarName, GenerateCodeBlock block) {
        binder.bindToStatement(stmtVarName, indexVarName, valueVarName, block);
    }

    @Override
    public void getArgsCount(String inputVarName, String outVarName, GenerateCodeBlock block) {
        throw new LightCompileException("Should not call getArgCount on basic adapters." +
                "It is always one.");
    }
}
