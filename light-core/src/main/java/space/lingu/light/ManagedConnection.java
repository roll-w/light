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

package space.lingu.light;

import space.lingu.light.util.RuntimeCloseable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author RollW
 */
public class ManagedConnection implements RuntimeCloseable {
    private static final byte[] DUMMY = new byte[0];

    private final LightDatabase database;
    private final Connection connection;
    private final LightDatabase.Metadata metadata;
    private final Map<PreparedStatement, byte[]> statements = new ConcurrentHashMap<>();

    public ManagedConnection(LightDatabase database) {
        this.database = database;
        this.connection = database.requireConnection();
        this.metadata = database.getMetadata();
    }

    public PreparedStatement acquire(String sql, boolean returnsGeneratedKey) {
        PreparedStatement stmt =
                database.resolveStatement(sql, connection, returnsGeneratedKey);
        statements.put(stmt, DUMMY);
        return stmt;
    }

    public PreparedStatement acquire(String sql) {
        return acquire(sql, false);
    }

    public LightDatabase.Metadata getMetadata() {
        return metadata;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Begin transaction if database supports.
     */
    public void beginTransaction() {
        if (notSupportTransaction()) {
            return;
        }
        autoCommit(false);
    }

    /**
     * Commit the current transaction if database supports,
     * and close the transaction.
     */
    public void commit() {
        if (notSupportTransaction()) {
            return;
        }
        if (connection == null) {
            return;
        }
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        } finally {
            autoCommit(true);
        }
    }

    /**
     * Rollback current transaction if database supports,
     * and close the transaction.
     */
    public void rollback() {
        if (notSupportTransaction()) {
            return;
        }
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        } finally {
            autoCommit(true);
        }
    }

    @Override
    public void close() throws LightRuntimeException {
        for (PreparedStatement preparedStatement : statements.keySet()) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                throw new LightRuntimeException(e);
            }
        }
        database.releaseConnection(connection);
    }

    public void release(PreparedStatement statement) throws LightRuntimeException {
        try {
            statements.remove(statement);
            statement.close();
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
    }

    private void autoCommit(boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
    }

    private boolean notSupportTransaction() {
        return !metadata.supportsTransaction;
    }
}
