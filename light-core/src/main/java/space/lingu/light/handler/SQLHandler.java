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
import space.lingu.light.LightRuntimeException;
import space.lingu.light.SharedConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Internal API. Handle custom SQL expression.
 *
 * @author RollW
 */
public class SQLHandler extends SharedConnection {
    public final String sql;
    protected final LightDatabase mDatabase;
    private volatile Connection mConnection;

    public SQLHandler(LightDatabase database, String sql) {
        super(database);
        this.sql = sql;
        mDatabase = database;
    }

    protected String replaceWithPlaceholders(int[] args) {
        if (args.length == 0) {
            return sql;
        }
        String[] placeholders = new String[args.length + 1];
        for (int n = 0; n < args.length; n++) {
            placeholders[n] = mDatabase
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
            builder.append(" (").append(placeholders[i]).append(") ");
            if (i != 0) {
                builder.append(unescaped, details.get(i - 1).end, detail.start);
            }
            if (i == details.size() - 1) {
                builder.append(unescaped, detail.end, unescaped.length());
            }
        }
        return builder.toString();
    }

    protected LightDatabase getDatabase() {
        return mDatabase;
    }

    public void endTransaction() {
        this.commit();
    }

    public PreparedStatement acquire(int[] args) {
        mConnection = acquire();
        return mDatabase.resolveStatement(replaceWithPlaceholders(args),
                mConnection, false);
    }

    public void release(PreparedStatement stmt) {
        try {
            stmt.close();
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        } finally {
            release(mConnection);
        }
    }
}
