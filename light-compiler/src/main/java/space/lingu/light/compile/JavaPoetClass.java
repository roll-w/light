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

package space.lingu.light.compile;

import com.squareup.javapoet.ClassName;
import space.lingu.light.Light;
import space.lingu.light.LightDatabase;
import space.lingu.light.SharedConnection;
import space.lingu.light.handler.DeleteUpdateHandler;
import space.lingu.light.handler.InsertHandler;
import space.lingu.light.handler.SQLHandler;
import space.lingu.light.util.ResultSetUtil;
import space.lingu.light.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author RollW
 */
public class JavaPoetClass {
    public static final String LIGHT_PACKAGE = "space.lingu.light";

    public static final ClassName LIGHT_DATABASE = ClassName.get(LightDatabase.class);
    public static final ClassName LIGHT = ClassName.get(Light.class);

    public static final ClassName SHARED_CONNECTION = ClassName.get(SharedConnection.class);

    public static final ClassName INSERT_HANDLER = ClassName.get(InsertHandler.class);
    public static final ClassName DELETE_UPDATE_HANDLER = ClassName.get(DeleteUpdateHandler.class);
    public static final ClassName SQL_HANDLER = ClassName.get(SQLHandler.class);

    public static class UtilNames {
        public static final ClassName STRING_UTIL = ClassName.get(StringUtil.class);
        public static final ClassName RESULT_SET_UTIL = ClassName.get(ResultSetUtil.class);

    }

    public static class JdbcNames {
        public static final ClassName PREPARED_STMT = ClassName.get(PreparedStatement.class);
        public static final ClassName RESULT_SET = ClassName.get(ResultSet.class);
    }

    public static class LangNames {
        public static final ClassName STRING = ClassName.get(String.class);
    }
}
