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
import space.lingu.light.struct.TableIndex;

/**
 * The database dialect provider.
 *
 * @author RollW
 */
public interface DialectProvider extends SQLEscaper {
    /**
     * Get the statement that created the table.
     * <p>
     * Output the SQL statement that creates the table structure
     * (primary key, foreign key, etc.),
     * but does not include the statement that creates the index.
     *
     * @param table {@link Table}
     * @return create table statement
     */
    default String create(Table table) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Get the statement that created the index.
     *
     * @param index {@link TableIndex}
     * @return create index statement
     */
    default String create(TableIndex index) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Get the statement that created the database.
     * <p>
     * Only returns statement that creates the database.
     *
     * @param databaseInfo {@link DatabaseInfo}
     * @return create database statement
     */
    default String create(DatabaseInfo databaseInfo) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default String destroy(Table table) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default String destroy(DatabaseInfo databaseInfo) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default String useDatabase(String databaseName) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default String use(DatabaseInfo databaseInfo) {
        throw new UnsupportedOperationException("Not implemented");
    }

    SQLGenerator getGenerator();
}
