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

/**
 * SQL数据类型
 * @author RollW
 */
public enum SQLDataType {
    INT, LONG, FLOAT,
    DOUBLE, BOOLEAN,
    CHAR, BINARY,
    /**
     * 文本<br>
     * MySQL中Text无法作为主键，当设定此类型值为主键时，更改为varchar(255)。
     */
    TEXT,
    /**
     * 不存在此类型数据的数据库，映射为Text
     */
    VARCHAR,
    LONGTEXT, BIG_DECIMAL,
    UNDEFINED
}
