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
import space.lingu.light.struct.DatabaseInfo;
import space.lingu.light.struct.Table;
import space.lingu.light.struct.TableColumn;
import space.lingu.light.struct.TableIndex;
import space.lingu.light.util.StringUtil;

/**
 * @author RollW
 */
public class MySQLDialectProvider extends AsciiSQLGenerator
        implements DialectProvider, SQLGenerator {
    public static final int DEFAULT_VARCHAR_LENGTH = 16383;
    public static final String DEFAULT_CHARSET = "utf8";
    public static final String DEFAULT_ENGINE = "InnoDB";

    public MySQLDialectProvider() {
    }

    // --------- DialectProvider ---------
    @Override
    public String create(Table table) {
        // TODO: create table
        table.getColumns().forEach(column -> {
        });
        return null;
    }

    @Override
    public String create(TableIndex index) {
        // TODO: create index
        return "";
    }

    private String createColumn(TableColumn column) {
        // TODO: create column
        return "";
    }

    @Override
    public String create(DatabaseInfo info) {
        return "CREATE DATABASE IF NOT EXISTS " + escapeParam(info.getName());
    }

    @Override
    public String useDatabase(String databaseName) {
        return "USE " + escapeParam(databaseName);
    }

    @Override
    public String destroy(Table table) {
        return "DROP TABLE IF EXISTS " + escapeParam(table.getName());
    }

    @Override
    public String destroy(DatabaseInfo databaseInfo) {
        return "DROP DATABASE IF EXISTS " + escapeParam(databaseInfo.getName());
    }

    @Override
    public String use(DatabaseInfo databaseInfo) {
        return useDatabase(databaseInfo.getName());
    }

    @Override
    public SQLGenerator getGenerator() {
        return this;
    }

    // --------- SQLGenerator ---------

    @Override
    public String insert(String tableName, OnConflictStrategy onConflict, String... valueArgs) {
        if (StringUtil.isEmpty(tableName)) {
            return null;
        }
        String start;
        switch (onConflict) {
            case REPLACE:
                start = "REPLACE";
                break;
            case IGNORE:
                start = "INSERT IGNORE";
                break;
            case ABORT:
            default:
                start = "INSERT";
        }
        return buildInsertWithStart(tableName, start, valueArgs);
    }

}
