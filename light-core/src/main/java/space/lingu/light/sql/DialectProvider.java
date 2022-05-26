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

package space.lingu.light.sql;

import space.lingu.light.struct.DatabaseInfo;
import space.lingu.light.struct.Table;

/**
 * 数据库方言提供
 * @author RollW
 */
public interface DialectProvider {
    /**
     * 创建表
     */
    String create(Table table);

    /**
     * 创建数据库
     */
    String create(DatabaseInfo info);

    String destroyTable(String tableName);

    String destroyDatabase(String databaseName);

    String useDatabase(String databaseName);

    SQLGenerator getGenerator();

    // TODO: 生成语句接口
}
