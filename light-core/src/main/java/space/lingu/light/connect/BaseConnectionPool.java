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
import space.lingu.light.LightLogger;
import space.lingu.light.log.LightEmptyLogger;

/**
 * @author RollW
 */
public abstract class BaseConnectionPool implements ConnectionPool {
    protected LightLogger logger;

    private volatile DatasourceConfig datasourceConfig;

    @Override
    public void setLogger(LightLogger logger) {
        if (logger == null) {
            this.logger = LightEmptyLogger.getInstance();
            return;
        }
        this.logger = logger;
    }

    @Override
    public LightLogger getLogger() {
        return logger;
    }

    @Override
    public DatasourceConfig getDatasourceConfig() {
        return datasourceConfig;
    }

    @Override
    public void setDatasourceConfig(DatasourceConfig datasourceConfig) {
        this.datasourceConfig = datasourceConfig;
    }
}
