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

/**
 * Combine two converters
 *
 * @author RollW
 */
public class CombinedTypeConverter extends TypeConverter {
    private final TypeConverter converter1, converter2;

    public CombinedTypeConverter(TypeConverter converter1, TypeConverter converter2) {
        super(converter1.from, converter2.to);
        this.converter1 = converter1;
        this.converter2 = converter2;
    }

    @Override
    protected String doConvert(String inVarName, GenerateCodeBlock block) {
        String out = converter1.convert(inVarName, block);
        return converter2.convert(out, block);
    }

    @Override
    protected void doConvert(String inVarName, String outVarName, GenerateCodeBlock block) {
        String c1out = converter1.convert(inVarName, block);
        converter2.convert(c1out, outVarName, block);
    }
}
