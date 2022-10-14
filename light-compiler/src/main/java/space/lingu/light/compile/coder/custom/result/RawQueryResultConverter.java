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

package space.lingu.light.compile.coder.custom.result;

import space.lingu.light.compile.JavaPoetClass;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.custom.row.NoOpRowConverter;
import space.lingu.light.compile.javac.ProcessEnv;

import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.Objects;

/**
 * Returns {@code ResultSet}
 *
 * @author RollW
 */
public class RawQueryResultConverter extends QueryResultConverter {
    protected RawQueryResultConverter(ProcessEnv env) {
        super(Collections.singletonList(createNoOpConverter(env)));
    }

    @Override
    public void convert(String outVarName, String resultSetName, GenerateCodeBlock block) {
        block.builder().addStatement("$T $L = $L",
                JavaPoetClass.JdbcNames.RESULT_SET, outVarName, resultSetName);
    }

    private static RawQueryResultConverter INSTANCE;

    public static RawQueryResultConverter create(ProcessEnv env) {
        if (INSTANCE == null) {
            INSTANCE = new RawQueryResultConverter(env);
        }
        return INSTANCE;
    }

    private static TypeMirror RESULT_SET_MIRROR;

    public static NoOpRowConverter createNoOpConverter(ProcessEnv env) {
        if (RESULT_SET_MIRROR == null) {
            RESULT_SET_MIRROR = env.getElementUtils()
                    .getTypeElement(JavaPoetClass.JdbcNames.RESULT_SET.canonicalName())
                    .asType();
        }
        return new NoOpRowConverter(RESULT_SET_MIRROR);
    }

    public static boolean isRaw(TypeMirror typeMirror, ProcessEnv env) {
        if (RESULT_SET_MIRROR == null) {
            RESULT_SET_MIRROR = env.getElementUtils()
                    .getTypeElement(JavaPoetClass.JdbcNames.RESULT_SET.canonicalName())
                    .asType();
        }
        return Objects.equals(typeMirror, RESULT_SET_MIRROR);
    }

}
