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

package space.lingu.light.connect.simple;

import space.lingu.light.DatasourceConfig;
import space.lingu.light.LightExperimentalApi;
import space.lingu.light.LightLogger;
import space.lingu.light.LightRuntimeException;
import space.lingu.light.connect.ConnectionPool;

import javax.sql.PooledConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author RollW
 */
@LightExperimentalApi
public class JdbcPooledConnectionPool implements ConnectionPool {
    private PooledConnection pooledConnection;
    private LightLogger logger = null;

    public JdbcPooledConnectionPool(PooledConnection connection) {
        this.pooledConnection = connection;
        if (pooledConnection == null) {
            throw new LightRuntimeException("PooledConnection cannot be null.");
        }
    }

    /**
     * Has no effect for the class.
     * If you want set config,
     * use {@link #setPooledConnection(PooledConnection)} instead.
     *
     * @param config {@link DatasourceConfig}
     */
    @Override
    public void setDatasourceConfig(DatasourceConfig config) {
        // has no effect
    }

    @Override
    public DatasourceConfig getDatasourceConfig() {
        return null;
    }

    /**
     * Set {@link PooledConnection} for the class.
     *
     * @param connection an instance of {@link PooledConnection}, cannot be null.
     */
    public void setPooledConnection(PooledConnection connection) {
        this.pooledConnection = connection;
        if (pooledConnection == null) {
            throw new LightRuntimeException("PooledConnection cannot be null.");
        }
    }

    public PooledConnection getPooledConnection() {
        return pooledConnection;
    }

    @Override
    public Connection requireConnection() {
        if (pooledConnection == null) {
            throw new LightRuntimeException("Not set yet.");
        }
        try {
            return pooledConnection.getConnection();
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
    }

    @Override
    public void release(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
    }

    @Override
    public void setLogger(LightLogger logger) {
        this.logger = logger;
    }

    @Override
    public LightLogger getLogger() {
        return logger;
    }

    @Override
    public void close() throws IOException {
        try {
            pooledConnection.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }
}
