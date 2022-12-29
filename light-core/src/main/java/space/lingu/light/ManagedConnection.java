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
 * Represent a connection and managed by Light.
 *
 * @author RollW
 */
@SuppressWarnings({"unused"})
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

    // Connection api

    /**
     * See {@link Connection#prepareStatement(String, int)}
     *
     * @see Connection#prepareStatement(String, int)
     */
    public PreparedStatement acquire(String sql, int autoGeneratedKeys) {
        PreparedStatement stmt;
        try {
            stmt = connection.prepareStatement(sql, autoGeneratedKeys);
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
        statements.put(stmt, DUMMY);
        return stmt;
    }

    /**
     * See {@link Connection#prepareStatement(String, int[])}
     *
     * @see Connection#prepareStatement(String, int[])
     */
    public PreparedStatement acquire(String sql, int[] columnIndexes) {
        PreparedStatement stmt;
        try {
            stmt = connection.prepareStatement(sql, columnIndexes);
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
        statements.put(stmt, DUMMY);
        return stmt;
    }

    /**
     * See {@link Connection#prepareStatement(String, String[])}
     *
     * @see Connection#prepareStatement(String, String[])
     */
    public PreparedStatement acquire(String sql, String[] columnNames) {
        PreparedStatement stmt;
        try {
            stmt = connection.prepareStatement(sql, columnNames);
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
        statements.put(stmt, DUMMY);
        return stmt;
    }

    /**
     * See {@link Connection#prepareStatement(String, int, int)}
     *
     * @see Connection#prepareStatement(String, int, int)
     */
    public PreparedStatement acquire(String sql, int resultSetType,
                                     int resultSetConcurrency,
                                     int resultSetHoldability) {
        PreparedStatement stmt;
        try {
            stmt = connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
        statements.put(stmt, DUMMY);
        return stmt;
    }

    /**
     * See {@link Connection#prepareStatement(String, int, int)}
     *
     * @see Connection#prepareStatement(String, int, int)
     */
    public PreparedStatement acquire(String sql, int resultSetType,
                                     int resultSetConcurrency) {
        PreparedStatement stmt;
        try {
            stmt = connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
        statements.put(stmt, DUMMY);
        return stmt;
    }

    public LightDatabase.Metadata getMetadata() {
        return metadata;
    }

    /**
     * Get the raw connection, but notice that you should not manually close it.
     * <p>
     * If you want open a {@link PreparedStatement}, you should use {@link #acquire(String)}
     * or other similar methods provided by this class instead. {@link PreparedStatement}
     * that you open manually will not be managed.
     *
     * @return the raw {@link Connection}
     */
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
     * Alias for {@link #commit()}.
     */
    public void endTransaction() {
        commit();
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

    /**
     * Release the connection, will also close all
     * {@link PreparedStatement}s on the connection.
     *
     * @throws LightRuntimeException if release connection failed.
     */
    @Override
    public void close() throws LightRuntimeException {
        for (PreparedStatement preparedStatement : statements.keySet()) {
            try {
                preparedStatement.close();
            } catch (SQLException ignored) {
            }
        }
        database.releaseConnection(connection);
    }

    /**
     * Release the statement.
     *
     * @throws LightRuntimeException if release statement failed.
     */
    public void release(PreparedStatement statement) throws LightRuntimeException {
        try {
            statements.remove(statement);
            statement.close();
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
    }

    /**
     * Alias for {@link #close()}.
     */
    public void release() throws LightRuntimeException {
        close();
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