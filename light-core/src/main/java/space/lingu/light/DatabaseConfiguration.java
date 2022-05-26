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

import space.lingu.light.connect.ConnectionPool;
import space.lingu.light.connect.DatasourceConfig;
import space.lingu.light.log.LightLogger;
import space.lingu.light.sql.DialectProvider;

import java.util.List;

/**
 * @author RollW
 */
public class DatabaseConfiguration {
    public final String name;
    public final DatasourceConfig datasourceConfig;
    public final Class<? extends ConnectionPool> connectionPoolClass;
    public final Class<? extends DialectProvider> dialectProviderClass;
    public final LightLogger logger;

    public DatabaseConfiguration(String name,
                                 DatasourceConfig datasourceConfig,
                                 Class<? extends ConnectionPool> connectionPoolClass,
                                 Class<? extends DialectProvider> dialectProviderClass,
                                 LightLogger logger) {
        this.name = name;
        this.datasourceConfig = datasourceConfig;
        this.connectionPoolClass = connectionPoolClass;
        this.dialectProviderClass = dialectProviderClass;
        this.logger = logger;
    }
}
