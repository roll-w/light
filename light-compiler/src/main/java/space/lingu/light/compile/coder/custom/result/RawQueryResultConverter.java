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
import space.lingu.light.compile.coder.custom.QueryContext;
import space.lingu.light.compile.coder.custom.row.NoOpRowConverter;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeCompileType;

import java.sql.ResultSet;
import java.util.Objects;

/**
 * Returns {@code ResultSet}
 *
 * @author RollW
 */
public class RawQueryResultConverter extends AbstractQueryResultConverter {
    protected RawQueryResultConverter(ProcessEnv env) {
        super(createNoOpConverter(env));
    }

    @Override
    public void convert(QueryContext queryContext, GenerateCodeBlock block) {
        block.builder().addStatement("$T $L = $L",
                JavaPoetClass.JdbcNames.RESULT_SET,
                queryContext.getOutVarName(),
                queryContext.getResultSetVarName()
        );
    }

    private static RawQueryResultConverter INSTANCE;

    public static RawQueryResultConverter create(ProcessEnv env) {
        if (INSTANCE == null) {
            INSTANCE = new RawQueryResultConverter(env);
        }
        return INSTANCE;
    }

    private static TypeCompileType RESULT_SET_TYPE;

    public static NoOpRowConverter createNoOpConverter(ProcessEnv env) {
        initResultSetMirror(env);
        return new NoOpRowConverter(RESULT_SET_TYPE);
    }

    private static void initResultSetMirror(ProcessEnv env) {
        if (RESULT_SET_TYPE == null) {
            RESULT_SET_TYPE = env.getTypeCompileType(ResultSet.class);
        }
    }

    public static boolean isRaw(TypeCompileType type,
                                ProcessEnv env) {
        initResultSetMirror(env);
        return Objects.equals(
                type.getTypeMirror(),
                RESULT_SET_TYPE.getTypeMirror()
        );
    }

}
