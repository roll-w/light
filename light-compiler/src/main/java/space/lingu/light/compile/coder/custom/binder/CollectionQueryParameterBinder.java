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

package space.lingu.light.compile.coder.custom.binder;

import com.squareup.javapoet.TypeName;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.StatementBinder;

/**
 * @author RollW
 */
public class CollectionQueryParameterBinder extends QueryParameterBinder {
    public final StatementBinder binder;

    public CollectionQueryParameterBinder(StatementBinder binder) {
        super(true);
        this.binder = binder;
    }

    @Override
    public void bindToStatement(String stmtVarName, String indexVarName, String valueVarName, GenerateCodeBlock block) {
        final String iterVar = block.getTempVar("_item");
        block.builder().beginControlFlow("for ($T $L : $L)",
                TypeName.get(binder.type()), iterVar, valueVarName);
        binder.bindToStatement(stmtVarName, indexVarName, iterVar, block);
        block.builder().addStatement("$L++", indexVarName)
                .endControlFlow();
    }

    @Override
    public void getArgsCount(String inputVarName, String outVarName, GenerateCodeBlock block) {
        block.builder().addStatement("final $T $L = $L.size()", TypeName.INT, outVarName, inputVarName);
    }
}
