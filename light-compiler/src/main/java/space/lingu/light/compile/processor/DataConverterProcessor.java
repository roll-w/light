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
import space.lingu.light.compile.javac.ElementUtils;
import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.javac.TypeUtils;
import space.lingu.light.compile.struct.DataConverter;

import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;

/**
 * @author RollW
 */
public class DataConverterProcessor implements Processor<DataConverter> {
    private final MethodCompileType methodCompileType;
    private final TypeCompileType containing;
    private final ProcessEnv env;

    public DataConverterProcessor(MethodCompileType methodCompileType,
                                  TypeCompileType containing,
                                  ProcessEnv env) {
        this.methodCompileType = methodCompileType;
        this.containing = containing;
        this.env = env;
    }

    // TODO: supports DataConverter different define domains
    //  Database, Table, Field, Dao, etc.

    @Override
    public DataConverter process() {
        if (!ElementUtils.isPublic(methodCompileType.getElement())) {
            env.getLog().error(
                    CompileErrors.DATA_CONVERTER_METHOD_NOT_PUBLIC,
                    methodCompileType
            );
        }
        if (!ElementUtils.isStatic(methodCompileType.getElement())) {
            env.getLog().error(
                    CompileErrors.DATA_CONVERTER_METHOD_NOT_STATIC,
                    methodCompileType
            );
        }
        if (methodCompileType.getParameters().size() > 1) {
            env.getLog().error(
                    CompileErrors.DATA_CONVERTER_TOO_MUCH_PARAMS,
                    methodCompileType
            );
        }
        if (methodCompileType.getParameters().isEmpty()) {
            env.getLog().error(
                    CompileErrors.DATA_CONVERTER_NO_PARAM,
                    methodCompileType
            );
        }

        TypeCompileType returnType = methodCompileType.getReturnType();
        if (isInvalidReturnType(returnType.getTypeMirror())) {
            env.getLog().error(
                    CompileErrors.DATA_CONVERTER_INVALID_RETURN_TYPE,
                    methodCompileType
            );
        }
        return new DataConverter(containing, methodCompileType,
                methodCompileType.getParameters().get(0).getType(),
                methodCompileType.getReturnType());
    }

    private static boolean isInvalidReturnType(TypeMirror returnType) {
        return TypeUtils.isError(returnType) ||
                TypeUtils.isVoid(returnType) ||
                TypeUtils.isNone(returnType);
    }

    public static List<DataConverter> findConverters() {
        return Collections.emptyList();
    }
}
