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

package space.lingu.light.handler;

import space.lingu.light.LightDatabase;
import space.lingu.light.ManagedConnection;
import space.lingu.light.util.ResultSetUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal API. Handle custom SQL expression.
 *
 * @author RollW
 */
public class SQLHandler {
    private final String sql;
    private final LightDatabase database;
    private final Map<String, ColumnIndex> columnIndexMap;

    public SQLHandler(LightDatabase database, String sql) {
        this(database, sql, Collections.emptyList());
    }

    public SQLHandler(LightDatabase database, String sql,
                      ColumnIndex... initialIndexes) {
        this(database, sql, Arrays.asList(initialIndexes));
    }

    public SQLHandler(LightDatabase database, String sql,
                      List<ColumnIndex> initialIndexes) {
        this.sql = sql;
        this.database = database;
        this.columnIndexMap = new ConcurrentHashMap<>();
        initialIndexes.forEach(index -> columnIndexMap.put(index.getName(), index));
    }

    protected String replaceWithPlaceholders(int[] args) {
        if (args.length == 0) {
            return sql;
        }
        String[] placeholders = new String[args.length + 1];
        for (int n = 0; n < args.length; n++) {
            placeholders[n] = database
                    .getDialectProvider()
                    .getGenerator()
                    .placeHolders(args[n]);
        }
        SQLExpressionParser parser = new SQLExpressionParser(sql);
        String unescaped = SQLExpressionParser.unescape(sql);
        List<SQLExpressionParser.Detail> details = parser.getDetails();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < details.size(); i++) {
            SQLExpressionParser.Detail detail = details.get(i);
            if (i == 0) {
                builder.append(unescaped, 0, detail.start);
            }
            builder.append(placeholders[i])
                    .append(unescaped, detail.end, nextStart(i, unescaped, details));
        }
        return builder.toString();
    }

    private int nextStart(int index, String unescaped,
                          List<SQLExpressionParser.Detail> details) {
        if (index + 1 >= details.size()) {
            return unescaped.length();
        }
        return details.get(index + 1).start;
    }

    public LightDatabase getDatabase() {
        return database;
    }

    public String getSql() {
        return sql;
    }

    public ManagedConnection newConnection() {
        return database.requireManagedConnection();
    }

    /**
     * Acquire a prepared statement with args.
     *
     * @param args number of template parameters in parameter order
     */
    public PreparedStatement acquire(ManagedConnection connection, int[] args) {
        return connection.acquire(replaceWithPlaceholders(args), false);
    }

    public void release(ManagedConnection connection) {
        connection.close();
    }

    /**
     * Get column index by name.
     *
     * @param name column name
     * @return the index of column, -1 if not found.
     */
    public int getColumnIndex(ResultSet resultSet, String name) {
        ColumnIndex columnIndex = columnIndexMap.get(name);
        if (columnIndex != null) {
            return columnIndex.getIndex();
        }
        int index = ResultSetUtils.getColumnIndexSwallow(resultSet, name);
        ColumnIndex newIndex = new ColumnIndex(index, name);
        columnIndexMap.put(name, newIndex);
        return index;
    }

    public static final class ColumnIndex {
        private final int index;
        private final String name;

        ColumnIndex(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }
    }
}
