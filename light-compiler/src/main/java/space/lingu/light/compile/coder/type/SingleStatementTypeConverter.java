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

import com.squareup.javapoet.CodeBlock;
import space.lingu.light.compile.coder.GenerateCodeBlock;

import javax.lang.model.type.TypeMirror;

/**
 * @author RollW
 */
public abstract class SingleStatementTypeConverter extends TypeConverter {
    public SingleStatementTypeConverter(TypeMirror from, TypeMirror to) {
        super(from, to);
    }

    @Override
    protected final String doConvert(String inVarName, GenerateCodeBlock block) {
        final String outVarName = block.getTempVar();
        block.builder()
                .addStatement("$L = $L", outVarName, buildStatement(inVarName, block));
        return outVarName;
    }

    @Override
    protected final void doConvert(String inVarName, String outVarName, GenerateCodeBlock block) {
        block.builder()
                .addStatement("$L = $L", outVarName, buildStatement(inVarName, block));
    }

    protected abstract CodeBlock buildStatement(String inputVar, GenerateCodeBlock block);
}
