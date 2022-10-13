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

package space.lingu.light.connect;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import space.lingu.light.DatasourceConfig;
import space.lingu.light.LightRuntimeException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Hikari Connection Pool implementation of {@link ConnectionPool}.
 * <p>
 * If you are using a different version of {@code HikariCP} or
 * just don't want to import any more dependencies,
 * you can simply copy this file to your project.
 * <p>
 * In most cases it will work fine.
 * @author RollW
 */
public class HikariConnectionPool extends BaseConnectionPool {
    private HikariDataSource source;

    public HikariConnectionPool() {
    }

    @Override
    public void setDataSourceConfig(DatasourceConfig config) {
        if (source != null) {
            return;
        }
        HikariConfig hikariConfig = new HikariConfig();
        setupHikariConfig(hikariConfig);

        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        source = new HikariDataSource(hikariConfig);
    }

    @Override
    public Connection requireConnection() {
        checkPool();
        try {
            return source.getConnection();
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
    }

    @Override
    public void release(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new LightRuntimeException(e);
        }
    }

    @Override
    public void close() {
        checkPool();
        if (source != null) {
            source.close();
        }
    }

    private void checkPool() {
        if (source == null) {
            throw new LightRuntimeException("Not initialize Hikari datasource");
        }
    }

    /**
     * Set up your personal configuration.
     *
     * @param config HikariConfig
     */
    protected void setupHikariConfig(HikariConfig config) {

    }
}
