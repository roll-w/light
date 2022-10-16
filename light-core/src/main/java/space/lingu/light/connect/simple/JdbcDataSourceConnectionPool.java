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
import space.lingu.light.util.StringUtil;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author RollW
 */
@LightExperimentalApi
public class JdbcDataSourceConnectionPool implements ConnectionPool {
    private final ConnectionPoolDataSource mPoolDataSource;
    private PooledConnection mConnection;
    private LightLogger logger = null;

    // todo
    public JdbcDataSourceConnectionPool(ConnectionPoolDataSource poolDataSource) {
        this.mPoolDataSource = poolDataSource;
        try {
            mConnection = mPoolDataSource.getPooledConnection();
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
    }

    @Override
    public void setDataSourceConfig(DatasourceConfig config) {
        if (StringUtil.isEmpty(config.getUsername()) ||
                StringUtil.isEmpty(config.getPassword())) {
            return;
        }
        try {
            mConnection = mPoolDataSource.getPooledConnection(
                    config.getUsername(),
                    config.getPassword());
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
    }

    @Override
    public Connection requireConnection() {
        if (mConnection == null) {
            throw new LightRuntimeException("Not set yet.");
        }
        try {
            return mConnection.getConnection();
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
        if (mConnection == null) {
            return;
        }
        try {
            mConnection.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }
}
