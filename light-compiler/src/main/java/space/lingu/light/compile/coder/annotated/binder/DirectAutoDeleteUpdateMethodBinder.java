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

package space.lingu.light.compile.coder.annotated.binder;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.annotated.translator.AutoDeleteUpdateMethodTranslator;
import space.lingu.light.compile.struct.Parameter;
import space.lingu.light.util.Pair;

import java.util.List;
import java.util.Map;

/**
 * @author RollW
 */
public class DirectAutoDeleteUpdateMethodBinder extends AutoDeleteUpdateMethodBinder {
    public DirectAutoDeleteUpdateMethodBinder(AutoDeleteUpdateMethodTranslator translator) {
        super(translator);
    }

    @Override
    public void writeBlock(List<Parameter> params,
                           Map<String, Pair<FieldSpec, TypeSpec>> handlers,
                           GenerateCodeBlock block) {
        translator.createMethodBody(params, handlers, block);
    }
}
