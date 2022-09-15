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
import space.lingu.light.struct.TableForeignKey;
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
    String create(Table table);

    /**
     * Get the statement that created the index.
     *
     * @param index {@link TableIndex}
     * @return create index statement
     */
    String create(TableIndex index);

    String create(TableForeignKey index);

    /**
     * Get the statement that created the database.
     * <p>
     * Only returns statement that creates the database.
     *
     * @param databaseInfo {@link DatabaseInfo}
     * @return create database statement
     */
    String create(DatabaseInfo databaseInfo);

    /**
     * Drop the table.
     *
     * @param table {@link Table}
     * @return drop table statement
     */
    String drop(Table table);

    /**
     * Drop the database
     *
     * @param databaseInfo {@link DatabaseInfo}
     * @return drop database statement
     */
    String drop(DatabaseInfo databaseInfo);

    /**
     * Use database.
     *
     * @param databaseName name of database
     * @return use database statement
     */
    String useDatabase(String databaseName);

    /**
     * Use database.
     *
     * @param databaseInfo {@link DatabaseInfo}
     * @return drop database statement
     */
    String use(DatabaseInfo databaseInfo);

    /**
     * Get the {@link SQLGenerator} of the database.
     *
     * @return {@link SQLGenerator} of the database
     */
    SQLGenerator getGenerator();
}
