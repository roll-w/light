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
import space.lingu.light.struct.DatabaseInfo;
import space.lingu.light.struct.Table;
import space.lingu.light.struct.TableColumn;
import space.lingu.light.struct.TableIndex;
import space.lingu.light.util.StringUtil;

import java.util.StringJoiner;

/**
 * SQLite Dialect Provider.
 *
 * @author RollW
 */
public class SQLiteDialectProvider extends GeneralDialectProvider
        implements DialectProvider, SQLGenerator {

    /**
     * Enable strict mode on the table.
     * <p>
     * Needs to be a boolean value. If {@code true} enable it.
     * Disabled by default.
     */
    public static final String KEY_ENABLE_STRICT = "Key.SQLite.Strict";


    @Override
    public String create(Table table) {
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(escapeParam(table.getName()))
                .append(" (");
        StringJoiner columnJoiner = new StringJoiner(", ");
        table.getColumns().forEach(column ->
                columnJoiner.add(createColumn(column)));

        StringJoiner primaryKeyJoiner = new StringJoiner(", ");
        for (TableColumn column : table.getPrimaryKey().columns) {
            primaryKeyJoiner.add(escapeParam(column.getName()));
        }
        builder.append(columnJoiner);
        if (!table.getPrimaryKey().columns.isEmpty()) {
            builder.append(", PRIMARY KEY (")
                    .append(primaryKeyJoiner)
                    .append(") ");
        }

        builder.append(") ");
        Configurations.Configuration strictConfig =
                table.getConfigurations().findConfiguration(KEY_ENABLE_STRICT);
        if (strictConfig != null && strictConfig.toBoolean()) {
            builder.append("STRICT ");
        }

        return builder.toString();
    }


    @Override
    protected String notNullDeclare() {
        return "NOT NULL";
    }

    @Override
    protected String autoIncrementDeclare() {
        return "AUTOINCREMENT";
    }

    @Override
    protected String getDefaultTypeDeclaration(SQLDataType dataType,
                                               Configurations configurations) {
        if (dataType == null) {
            throw new IllegalArgumentException("SQLDataType is null. " +
                    "This may be a Light bug, please report it to us.");
        }
        String type = configurations.findConfigurationValue(
                LightConfiguration.KEY_COLUMN_TYPE);
        if (!StringUtil.isEmpty(type)) {
            return type;
        }

        switch (dataType) {
            case CHAR:
            case INT:
            case TINYINT:
            case SMALLINT:
            case LONG:
                return "INTEGER";
            case BOOLEAN:
                return "BOOLEAN";
            case FLOAT:
            case DOUBLE:
            case REAL:
                return "REAL";
            case CHARS:
            case VARCHAR:
            case LONGTEXT:
            case TEXT:
                return "TEXT";
            case BINARY:
                return "BLOB";
            default:
                throw new IllegalArgumentException("SQLDataType is undefined. " +
                        "This may be a Light bug, please report it to us.");
        }
    }

    @Override
    public String create(TableIndex index) {
        StringBuilder builder = new StringBuilder("CREATE");
        if (index.isUnique()) {
            builder.append(" UNIQUE");
        }
        builder.append(" INDEX IF NOT EXISTS ")
                .append(escapeParam(index.getName()))
                .append(" ON ")
                .append(escapeParam(index.getTableName()))
                .append(" (");
        StringJoiner indexColumns = new StringJoiner(", ");
        String[] columns = index.getColumns();
        boolean ordersEmpty = index.getOrders().length == 0;

        for (int i = 0; i < columns.length; i++) {
            indexColumns.add(escapeParam(columns[i]) +
                    (ordersEmpty
                            ? ""
                            : " " + getOrderOrDefault(i, index.getOrders())
                    ));
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

    @Override
    public String drop(Table table) {
        return "DROP TABLE IF EXISTS " + escapeParam(table.getName());
    }

    @Override
    public String create(DatabaseInfo databaseInfo) {
        // for SQLite, there is no "CREATE DATABASE" like command,
        // needs to be created a db file manually or automatically
        // created by sqlite when set up a connection.
        return null;
    }

    @Override
    public String drop(DatabaseInfo databaseInfo) {
        // also no "DROP DATABASE" like command,
        // needs to delete database file manually.
        return null;
    }

    @Override
    public String useDatabase(String databaseName) {
        // the same reason.
        return null;
    }

    @Override
    public String use(DatabaseInfo databaseInfo) {
        return null;
    }

    @Override
    public SQLGenerator getGenerator() {
        return this;
    }

    @Override
    public String insert(String tableName, String... valueArgs) {
        return insert(tableName, OnConflictStrategy.ABORT, valueArgs);
    }

    @Override
    public String insert(String tableName, OnConflictStrategy onConflict, String... valueArgs) {
        StringBuilder statementBuilder = new StringBuilder();
        if (onConflict == OnConflictStrategy.REPLACE) {
            statementBuilder.append("REPLACE");
        } else {
            statementBuilder.append("INSERT OR ").append(onConflict.name());
        }

        return buildInsertWithStart(tableName, statementBuilder.toString(), valueArgs);
    }

    @Override
    public String update(String tableName, String[] whereConditions, String[] valueArgs) {
        return update(tableName, OnConflictStrategy.ABORT, whereConditions, valueArgs);
    }

    @Override
    public String update(String tableName, OnConflictStrategy onConflict, String[] whereConditions, String[] valueArgs) {
        return buildUpdateWithStart(tableName,
                "UPDATE OR " + onConflict.name(),
                whereConditions, valueArgs);
    }
}
