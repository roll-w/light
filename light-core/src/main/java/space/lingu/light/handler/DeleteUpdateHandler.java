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
import space.lingu.light.ManagedConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Handler for delete or update.
 *
 * @author RollW
 */
@SuppressWarnings({"unused"})
public abstract class DeleteUpdateHandler<T> extends Handler<T> {

    public DeleteUpdateHandler(LightDatabase database) {
        super(database);
    }

    @Override
    protected abstract String createQuery();

    public final int handle(T entity) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquire(conn);
        try {
            bind(stmt, entity);
            conn.beginTransaction();
            return stmt.executeUpdate();
        } catch (SQLException e) {
            printError(e);
            throw new LightRuntimeException(e);
        } finally {
            conn.commit();
            conn.close();
        }
    }

    public final int handleMultiple(T[] entities) {
        return handleMultiple(Arrays.asList(entities));
    }

    public final int handleMultiple(Iterable<? extends T> entities) {
        final ManagedConnection conn = newConnection();
        final PreparedStatement stmt = acquire(conn);
        try {
            int count = 0;
            for (T entity : entities) {
                bind(stmt, entity);
                conn.beginTransaction();
                count += stmt.executeUpdate();
                conn.commit();
            }
            return count;
        } catch (SQLException e) {
            printError(e);
            throw new LightRuntimeException(e);
        } finally {
            conn.close();
        }
    }

    private void printError(Throwable throwable) {
        if (database.getLogger() == null) {
            return;
        }
        database.getLogger().error("An error occurred while execute delete or update.", throwable);
    }

}
