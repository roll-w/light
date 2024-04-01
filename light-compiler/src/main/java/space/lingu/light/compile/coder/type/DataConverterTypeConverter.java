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
import space.lingu.light.compile.struct.DataConverter;

/**
 * @author RollW
 */
public class DataConverterTypeConverter extends SingleStatementTypeConverter {
    private final DataConverter converter;

    public DataConverterTypeConverter(DataConverter dataConverter) {
        super(dataConverter.getFromType().getTypeMirror(),
                dataConverter.getToType().getTypeMirror());
        converter = dataConverter;
    }

    @Override
    protected CodeBlock buildStatement(String inputVar, GenerateCodeBlock block) {
        // may support non-static method future
        return CodeBlock.of("$T.$L($L)",
                converter.getEnclosingClass().toTypeName(),
                converter.getMethodName(),
                inputVar);
    }

    @Override
    public String toString() {
        return "DataConverterTypeConverter{" +
                "mConverter=" + converter +
                '}';
    }
}
