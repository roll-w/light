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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author RollW
 */
public class SQLHandler extends SharedConnection {
    private static final String REGEX = "\\{[_a-zA-Z]\\w+}";
    private static final Pattern sPattern = Pattern.compile(REGEX);

    public final String sql;
    protected final LightDatabase mDatabase;
    private volatile Connection mConnection;
    private final String[] argOrder;

    public SQLHandler(LightDatabase database, String sql, String... argOrder) {
        super(database);
        this.sql = sql;
        mDatabase = database;
        this.argOrder = argOrder;
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
        Matcher matcher = sPattern.matcher(sql);
        StringBuilder builder = new StringBuilder(sPattern.split(sql)[0]);
        while (matcher.find()) {
            String find = matcher.group();
            String name = find.substring(1, find.length() - 1);
            int index = find(argOrder, name);
            if (index < 0) {
                throw new LightRuntimeException("Unable to find parameter, " +
                        "please check SQL statement in annotation.");
            }
            builder.append("(").append(placeholders[index]).append(")");
        }
        return builder.toString();
    }

    protected int find(String[] args, String toFind) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(toFind)) {
                return i;
            }
        }
        return -1;
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
