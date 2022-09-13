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

package space.lingu.light.util;

import space.lingu.light.LightRuntimeException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link ResultSet} utility methods
 *
 * @author RollW
 */
public final class ResultSetUtil {

    public static int getColumnIndexOrThrow(ResultSet set, String name) {
        try {
            int index = getColumnIndex(set, name);
            if (index >= 0) return index;
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }

        throw new LightRuntimeException(
                new IllegalArgumentException("column '" + name + "' does not exist."));
    }

    public static int getColumnIndex(ResultSet set, String name) throws SQLException {
        int index = set.findColumn(name);
        if (index >= 0) {
            return index;
        }
        index = set.findColumn("`" + name + "`");
        return index;
    }

    public static int getColumnIndexSwallow(ResultSet set, String name) {
        try {
            int index = getColumnIndex(set, name);
            if (index >= 0) {
                return index;
            }
        } catch (SQLException ignored) {
            // swallow exception
        }
        return -1;
    }

    public static int getResultSetSize(ResultSet set) {
        try {
            set.last();
            set.getFetchSize();
            int size = set.getRow();
            set.beforeFirst();
            return size;
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
    }

    private ResultSetUtil() {
    }

}
