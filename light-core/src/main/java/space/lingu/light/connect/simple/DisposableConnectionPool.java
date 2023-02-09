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
import space.lingu.light.LightRuntimeException;
import space.lingu.light.connect.BaseConnectionPool;
import space.lingu.light.connect.ConnectionPool;
import space.lingu.light.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Disposable connection pool.
 * <p>
 * Not recommended for use in production environments.
 *
 * @author RollW
 */
public class DisposableConnectionPool extends BaseConnectionPool implements ConnectionPool {
    private DatasourceConfig mDatasourceConfig;

    public DisposableConnectionPool(DatasourceConfig config) {
        mDatasourceConfig = config;
    }

    public DisposableConnectionPool() {
    }

    @Override
    public void setDataSourceConfig(DatasourceConfig config) {
        mDatasourceConfig = config;
    }

    @Override
    public Connection requireConnection() {
        if (mDatasourceConfig == null || mDatasourceConfig.getUrl() == null) {
            throw new NullPointerException("DataSourceConfig is null or URL is null.");
        }
        try {
            Class.forName(mDatasourceConfig.getJdbcName());
        } catch (ClassNotFoundException e) {
            throw new LightRuntimeException("Jdbc driver class not found, please check properties.", e);
        }
        if (StringUtils.isEmpty(mDatasourceConfig.getPassword()) ||
                StringUtils.isEmpty(mDatasourceConfig.getUsername())) {
            try {
                return DriverManager.getConnection(mDatasourceConfig.getUrl());
            } catch (SQLException e) {
                if (logger != null)
                    logger.error(e);
                throw new LightRuntimeException(e);
            }
        }
        try {
            return DriverManager.getConnection(mDatasourceConfig.getUrl(),
                    mDatasourceConfig.getUsername(), mDatasourceConfig.getPassword());
        } catch (SQLException e) {
            if (logger != null)
                logger.error(e);
            throw new LightRuntimeException(e);
        }
    }

    @Override
    public void release(Connection connection) {
        if (connection == null) return;
        try {
            connection.close();
        } catch (SQLException e) {
            if (logger != null)
                logger.error(e);
            throw new LightRuntimeException(e);
        }
    }

    @Override
    public void close() {
    }
}
