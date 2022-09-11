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

package space.lingu.light.sql;

import space.lingu.light.OnConflictStrategy;

/**
 * SQL statement generator interface.
 *
 * @author RollW
 */
public interface SQLGenerator extends SQLEscaper {

    /**
     * Return insertion statement.
     *
     * @param tableName table name
     * @param valueArgs column names (in order)
     * @return insertion statement.
     */
    String insert(String tableName, String... valueArgs);

    String insert(String tableName, OnConflictStrategy onConflict, String... valueArgs);

    String delete(String tableName, String... conditions);

    String update(String tableName, String[] whereConditions, String[] valueArgs);

    String update(String tableName, OnConflictStrategy onConflict, String[] whereConditions, String[] valueArgs);

    default String placeHolders(int argsCount) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < argsCount; i++) {
            builder.append("?");
            if (i < argsCount - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

}
