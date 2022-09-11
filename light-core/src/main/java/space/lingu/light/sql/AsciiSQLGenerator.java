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
import space.lingu.light.util.StringUtil;

import java.util.StringJoiner;

/**
 * Standard ASCII SQL statement generator.
 * <p>
 * Also, can use as a base class for other database extensions.
 *
 * @author RollW
 */
public class AsciiSQLGenerator implements SQLGenerator {
    public AsciiSQLGenerator() {
    }

    @Override
    public String insert(String tableName, String... valueArgs) {
        return insert(tableName, OnConflictStrategy.ABORT, valueArgs);
    }

    @Override
    public String insert(String tableName, OnConflictStrategy onConflict, String... valueArgs) {
        // INSERT INTO `tableName` (`valueArg1`, `valueArg2`) VALUES (?, ?)
        return buildInsertWithStart(tableName, "INSERT", valueArgs);
    }

    protected String buildInsertWithStart(String tableName, String start, String... valueArgs) {
        if (StringUtil.isEmpty(tableName)) {
            return null;
        }
        StringBuilder builder = new StringBuilder(start)
                .append(" INTO ")
                .append(escapeParam(tableName))
                .append(" (");
        StringJoiner valueArgJoiner = new StringJoiner(", ");
        for (String valueArg : valueArgs) {
            valueArgJoiner.add(escapeParam(valueArg));
        }
        builder.append(valueArgJoiner).append(") VALUES ");

        if (valueArgs.length == 1) {
            builder.append("(?)");
            return builder.toString();
        }

        for (int i = 0; i < valueArgs.length; i++) {
            if (i == 0) {
                builder.append("(?");
            } else if (i < valueArgs.length - 1) {
                builder.append(", ?");
            } else {
                builder.append(", ?)");
            }
        }

        return builder.toString();
    }

    @Override
    public String delete(String tableName, String... conditions) {
        // DELETE FROM `tableName` WHERE `condition1` =? AND `condition2` =?
        if (StringUtil.isEmpty(tableName)) {
            return null;
        }
        StringBuilder builder = new StringBuilder("DELETE FROM ")
                .append(escapeParam(tableName)).append(" ");
        if (conditions.length == 0) {
            return builder.toString();
        }
        builder.append("WHERE ");
        StringJoiner conditionJoiner = new StringJoiner("AND ");
        for (String condition : conditions) {
            conditionJoiner.add(escapeParam(condition) + "=? ");
        }

        return builder.toString();
    }

    @Override
    public String update(String tableName, String[] whereConditions, String[] valueArgs) {
        return buildUpdateWithStart(tableName, "UPDATE", whereConditions, valueArgs);
    }

    @Override
    public String update(String tableName, OnConflictStrategy onConflict, String[] whereConditions, String[] valueArgs) {
        return update(tableName, whereConditions, valueArgs);
    }

    protected String buildUpdateWithStart(String tableName, String start, String[] whereConditions, String[] valueArgs) {
        if (StringUtil.isEmpty(tableName) || whereConditions == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(start)
                .append(" ")
                .append(escapeParam(tableName))
                .append(" ");
        if (valueArgs.length == 0) {
            return builder.toString();
        }
        builder.append("SET ");
        StringJoiner valueJoiner = new StringJoiner(", ");
        for (String valueArg : valueArgs) {
            valueJoiner.add(escapeParam(valueArg) + "=?");
        }
        builder.append(valueJoiner);
        if (whereConditions.length == 0) {
            return builder.toString();
        }
        builder.append("WHERE ");
        StringJoiner whereJoiner = new StringJoiner("AND ");
        for (String whereCondition : whereConditions) {
            whereJoiner.add(whereCondition + "=? ");
        }
        return builder.toString();
    }
}
