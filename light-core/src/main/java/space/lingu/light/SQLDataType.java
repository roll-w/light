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
 * SQL data types
 *
 * @author RollW
 */
public enum SQLDataType {
    /**
     * Tiny int.
     */
    TINYINT,
    /**
     * Small int.
     */
    SMALLINT,
    /**
     * int/integer.
     */
    INT,
    /**
     * long, or big int.
     */
    LONG,
    /**
     * Real. (float)
     */
    REAL,
    /**
     * float.
     */
    FLOAT,
    /**
     * double.
     */
    DOUBLE,
    /**
     * Decimal.
     */
    DECIMAL,
    /**
     * boolean.
     */
    BOOLEAN,
    /**
     * Date. May not be supported by all databases.
     */
    DATE,
    /**
     * Time. May not be supported by all databases.
     */
    TIME,
    /**
     * Timestamp. May not be supported by all databases.
     * <p>
     * Light supports convert long/Long type to timestamp by
     * indicating {@link DataColumn#dataType()}.
     */
    TIMESTAMP,
    /**
     * Primitive type {@code char}.
     */
    CHAR,
    /**
     * Fixed length string.
     */
    CHARS,
    /**
     * Binary data, or called blob.
     */
    BINARY,
    /**
     * Text.
     */
    TEXT,
    /**
     * Variable text.
     * <p>
     * Prefer choose this than {@link #TEXT} if available.
     * No database exists for this type of data, mapped as {@link #TEXT}.
     */
    VARCHAR,
    /**
     * Long text.
     */
    LONGTEXT,
    /**
     * Undefined. Not use this in your program.
     */
    UNDEFINED;

    public boolean isStringType() {
        return this == CHARS || this == TEXT ||
                this == VARCHAR || this == LONGTEXT;
    }
}
