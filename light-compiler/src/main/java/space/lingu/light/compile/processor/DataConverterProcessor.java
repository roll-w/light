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

import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeUtil;
import space.lingu.light.compile.struct.DataConverter;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author RollW
 */
public class DataConverterProcessor implements Processor<DataConverter> {
    private final TypeElement mContaining;
    private final ExecutableElement mExecutable;
    private final ProcessEnv mEnv;

    public DataConverterProcessor(ExecutableElement executable,
                                  TypeElement containing,// 包含此方法的类
                                  ProcessEnv env) {
        mContaining = containing;
        mExecutable = executable;
        mEnv = env;
    }


    @Override
    public DataConverter process() {
        if (!ElementUtil.isPublic(mExecutable)) {
            mEnv.getLog().error(
                    CompileErrors.DATA_CONVERTER_METHOD_NOT_PUBLIC,
                    mExecutable
            );
        }
        if (!ElementUtil.isStatic(mExecutable)) {
            mEnv.getLog().error(
                    CompileErrors.DATA_CONVERTER_METHOD_NOT_STATIC,
                    mExecutable
            );
        }

        DataConverter converter = new DataConverter();
        if (mExecutable.getParameters().size() > 1) {
            mEnv.getLog().error(
                    CompileErrors.DATA_CONVERTER_TOO_MUCH_PARAMS,
                    mExecutable
            );
        }
        if (mExecutable.getParameters().isEmpty()) {
            mEnv.getLog().error(
                    CompileErrors.DATA_CONVERTER_NO_PARAM,
                    mExecutable
            );
        }

        TypeMirror returnType = mExecutable.getReturnType();
        if (isInvalidReturnType(returnType)) {
            mEnv.getLog().error(
                    CompileErrors.DATA_CONVERTER_INVALID_RETURN_TYPE,
                    mExecutable
            );
        }

        return converter
                .setElement(mExecutable)
                .setEnclosingClass(mContaining)
                .setFromType(mExecutable.getParameters().get(0).asType())
                .setToType(mExecutable.getReturnType());
    }

    private static boolean isInvalidReturnType(TypeMirror returnType) {
        return TypeUtil.isError(returnType) ||
                TypeUtil.isVoid(returnType) ||
                TypeUtil.isNone(returnType);
    }
}
