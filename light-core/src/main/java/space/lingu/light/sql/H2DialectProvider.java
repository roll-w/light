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

/**
 * Temporary solution to support H2 database.
 * <p>
 * Set to MySQL mode by {@code SET MODE MYSQL} to
 * temporarily circumvent SQL syntax errors.
 *
 * @author RollW
 */
public class H2DialectProvider extends MySQLDialectProvider {
    @Override
    public String create(DatabaseInfo info) {
        return null;
    }

    @Override
    public String useDatabase(String databaseName) {
        return "SET MODE MYSQL";
    }

    @Override
    public String use(DatabaseInfo databaseInfo) {
        return useDatabase(databaseInfo.getName());
    }
}