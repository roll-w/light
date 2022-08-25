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
import space.lingu.light.log.LightLogger;

import java.io.Closeable;
import java.sql.Connection;

/**
 * 实现此接口自定义连接池。
 * @author RollW
 */
public interface ConnectionPool extends Closeable {
    /**
     * 设置数据来源配置
     * @param config {@link DatasourceConfig}
     */
    void setDataSourceConfig(DatasourceConfig config);

    /**
     * 从连接池获取连接
     * @return {@link Connection}
     */
    Connection requireConnection();

    /**
     * 释放连接，回到连接池
     * @param connection {@link Connection}
     */
    void release(Connection connection);

    void setLogger(LightLogger logger);

    LightLogger getLogger();
}
