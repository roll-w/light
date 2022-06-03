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

import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.DataConverter;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * @author RollW
 */
public class DataConverterProcessor implements Processor<DataConverter> {
    private final TypeElement mContaining;
    private final ExecutableElement mExecutable;
    private final ProcessEnv mEnv;

    private final DataConverter converter = new DataConverter();

    public DataConverterProcessor(ExecutableElement executable,
                                  TypeElement containing,// 包含此方法的类
                                  ProcessEnv env) {
        mContaining = containing;
        mExecutable = executable;
        mEnv = env;
    }


    @Override
    public DataConverter process() {
        if (mExecutable.getParameters().size() > 1) {
            throw new LightCompileException("A DataConverter method can only have one parameter.");
        }
        if (mExecutable.getParameters().isEmpty()) {
            throw new LightCompileException("A DataConverter method must have one parameter.");
        }

        return converter.setElement(mExecutable)
                .setEnclosingClass(mContaining)
                .setFromType(mExecutable.getParameters().get(0).asType())
                .setToType(mExecutable.getReturnType());
    }
}
