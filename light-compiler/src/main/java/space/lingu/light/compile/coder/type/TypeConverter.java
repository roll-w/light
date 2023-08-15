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

import space.lingu.light.compile.coder.GenerateCodeBlock;

import javax.lang.model.type.TypeMirror;

/**
 * @author RollW
 */
public abstract class TypeConverter {
    public final TypeMirror from;
    public final TypeMirror to;

    public TypeConverter(TypeMirror from, TypeMirror to) {
        this.from = from;
        this.to = to;
    }

    protected abstract void doConvert(String inVarName, String outVarName,
                                      GenerateCodeBlock block);

    protected String doConvert(String inVarName,
                               GenerateCodeBlock block) {
        String outVarName = block.getTempVar();
        doConvert(inVarName, outVarName, block);
        return outVarName;
    }

    public final void convert(String inVarName, String outVarName,
                              GenerateCodeBlock block) {
        doConvert(inVarName, outVarName, block);
    }

    public final String convert(String inVarName,
                                GenerateCodeBlock block) {
        return doConvert(inVarName, block);
    }
}
