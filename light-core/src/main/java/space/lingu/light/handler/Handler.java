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
import space.lingu.light.LightLogger;
import space.lingu.light.ManagedConnection;

import java.sql.PreparedStatement;

/**
 * SQL handler.
 *
 * @author RollW
 */
public abstract class Handler<T> {
    protected final LightDatabase database;

    public Handler(LightDatabase database) {
        this.database = database;
    }

    protected ManagedConnection newConnection() {
        return database.requireManagedConnection();
    }

    /**
     * Bind entity's parameters to statement.
     */
    protected abstract void bind(PreparedStatement statement, T entity);

    /**
     * The sql will be executed.
     *
     * @return the sql will be executed
     */
    protected abstract String createQuery();

    protected PreparedStatement acquire(ManagedConnection connection) {
        String sql = createQuery();
        printDebug("Execute: " + sql);
        return connection.acquire(sql);
    }

    protected PreparedStatement acquireReturnsGenerateKey(ManagedConnection connection) {
        String sql = createQuery();
        printDebug("Execute: " + sql);
        return connection.acquire(sql, true);
    }

    private void printDebug(String s) {
        LightLogger logger = database.getLogger();
        if (logger == null) {
            return;
        }
        logger.debug(s);
    }
}
