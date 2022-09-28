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

import space.lingu.light.*;
import space.lingu.light.struct.*;
import space.lingu.light.util.StringUtil;

import java.util.StringJoiner;

/**
 * @author RollW
 */
public class MySQLDialectProvider extends AsciiSQLGenerator
        implements DialectProvider, SQLGenerator {
    public static final String DEFAULT_VARCHAR_LENGTH = "16383";
    public static final String CHARSET_UTF8 = "utf8";
    public static final String CHARSET_UTF8MB4 = "utf8mb4";

    public static final String DEFAULT_CHARSET = CHARSET_UTF8MB4;
    public static final String DEFAULT_ENGINE = "InnoDB";

    public MySQLDialectProvider() {
    }

    // --------- DialectProvider ---------
    @Override
    public String create(Table table) {
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(escapeParam(table.getName()))
                .append(" ( ");
        StringJoiner columnJoiner = new StringJoiner(", ");
        table.getColumns().forEach(column ->
                columnJoiner.add(createColumn(column)));
        final String engine = table.getConfigurations().findConfigurationValue(
                LightConfiguration.KEY_ENGINE, DEFAULT_ENGINE);
        final String charset = table.getConfigurations().findConfigurationValue(
                LightConfiguration.KEY_CHARSET, DEFAULT_CHARSET);

        String autoIncrementStart = null;
        StringJoiner primaryKeyJoiner = new StringJoiner(", ");
        for (TableColumn column : table.getPrimaryKey().columns) {
            if (column.isHasDefaultValue()) {
                autoIncrementStart = column.getDefaultValue();
            }
            primaryKeyJoiner.add(escapeParam(column.getName()));
        }
        builder.append(columnJoiner);
        if (!table.getPrimaryKey().columns.isEmpty()) {
            builder.append(", PRIMARY KEY (")
                    .append(primaryKeyJoiner)
                    .append(") ");
        }

        builder.append(") ");
        if (autoIncrementStart != null) {
            builder.append(" AUTO_INCREMENT=")
                    .append(autoIncrementStart);
        }
        builder.append(" ENGINE=").append(engine)
                .append(" ")
                .append("DEFAULT CHARSET=")
                .append(charset);
        return builder.toString();
    }

    @Override
    public String create(TableIndex index) {
        // OR catching
        StringBuilder builder = new StringBuilder("ALTER TABLE ")
                .append(escapeParam(index.getTableName()))
                .append(" ADD ");
        if (index.isUnique()) {
            builder.append(" UNIQUE ");
        }
        builder.append(" INDEX ")
                .append("(");
        StringJoiner indexColumns = new StringJoiner(", ");
        String[] columns = index.getColumns();
        for (int i = 0; i < columns.length; i++) {
            indexColumns.add(escapeParam(columns[i]) + " " + getOrderOrDefault(i, index.getOrders()));
        }
        builder.append(indexColumns).append(") ");
        return builder.toString();
    }

    private String getOrderOrDefault(int index, Order[] orders) {
        try {
            return orders[index].name();
        } catch (Exception e) {
            return Order.ASC.name();
        }
    }

    private String createForeignKey(TableForeignKey index) {
        return "";
    }

    private String createColumn(TableColumn column) {
        StringBuilder builder = new StringBuilder(escapeParam(column.getName()))
                .append(" ");
        final String nonNull = column.isNullable()
                ? " "
                : " NON NULL ";
        builder.append(getDefaultTypeDeclaration(column.getDataType(),
                        column.getConfigurations()))
                .append(" ")
                .append(nonNull);
        if (column.isAutoGenerate()) {
            builder.append(" AUTO_INCREMENT ");
        }
        if (column.isHasDefaultValue()) {
            builder.append(" DEFAULT ");
            if (column.isDefaultValueNull()) {
                builder.append(" NULL ");
            } else {
                builder.append(column.getDefaultValueWithProcess());
            }
        }

        return builder.toString();
    }

    private String getDefaultTypeDeclaration(SQLDataType dataType,
                                             Configurations configurations) {
        if (dataType == null || dataType == SQLDataType.UNDEFINED) {
            throw new IllegalArgumentException("SQLDataType is null or undefined. " +
                    "This may be a Light bug, please report it to us.");
        }
        String type = configurations.findConfigurationValue(
                LightConfiguration.KEY_COLUMN_TYPE);
        if (!StringUtil.isEmpty(type)) {
            return type;
        }

        String size = configurations.findConfigurationValue(LightConfiguration.KEY_VARCHAR_LENGTH, DEFAULT_VARCHAR_LENGTH);
        switch (dataType) {
            case CHAR:
            case INT:
                return "INT";
            case TINYINT:
                return "TINYINT";
            case SMALLINT:
                return "SMALLINT";
            case LONG:
                return "BIGINT";
            case BOOLEAN:
                return "BOOL";
            case FLOAT:
                return "FLOAT";
            case DOUBLE:
                return "DOUBLE";
            case REAL:
                return "REAL";
            case CHARS:
                return "CHAR(" + size + ")";
            case VARCHAR:
                return "VARCHAR(" + size + ")";
            case TEXT:
                return "TEXT";
            case LONGTEXT:
                return "LONGTEXT";
            case BINARY:
                return "BLOB";
            default:
                throw new IllegalArgumentException("SQLDataType is null or undefined. " +
                        "This may be a Light bug, please report it to us.");
        }
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
    public String drop(Table table) {
        return "DROP TABLE IF EXISTS " + escapeParam(table.getName());
    }

    @Override
    public String drop(DatabaseInfo databaseInfo) {
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
