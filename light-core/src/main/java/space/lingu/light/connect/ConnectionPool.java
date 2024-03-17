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

import space.lingu.light.DatasourceConfig;
import space.lingu.light.LightDatabase;
import space.lingu.light.LightLogger;
import space.lingu.light.LightRuntimeException;

import java.io.Closeable;
import java.sql.Connection;

/**
 * Connection pool for Light.
 * It should be managed by the {@link LightDatabase}.
 *
 * @author RollW
 */
public interface ConnectionPool extends Closeable {
    /**
     * Set datasource configuration.
     *
     * @param config {@link DatasourceConfig}
     */
    void setDatasourceConfig(DatasourceConfig config);

    /**
     * Get datasource configuration.
     *
     * @return {@link DatasourceConfig}
     */
    DatasourceConfig getDatasourceConfig();

    /**
     * Require connection from connection pool.
     *
     * @return {@link Connection}
     */
    Connection requireConnection();

    /**
     * Release connection.
     *
     * @param connection {@link Connection}
     * @throws LightRuntimeException if connection release failed.
     */
    void release(Connection connection) throws LightRuntimeException;

    /**
     * Set logger.
     *
     * @param logger a {@link LightLogger}
     */
    void setLogger(LightLogger logger);

    /**
     * Get the {@link LightLogger}.
     *
     * @return logger (nullable)
     */
    LightLogger getLogger();
}
