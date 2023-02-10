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

import java.lang.annotation.*;

/**
 * Marks as a column in the data table.
 *
 * @author RollW
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface DataColumn {
    /**
     * The column name in the table, puts it empty to apply the field name.
     *
     * @return the column name
     */
    String name() default "";

    /**
     * The comment/description of the column.
     * <p>
     * Add comment to the database is not yet supported.
     *
     * @return comment/description
     */
    String description() default "";

    /**
     * Whether to set an index on this column.
     *
     * @return {@code true} if sets an index
     */
    boolean index() default false;

    /**
     * Whether this column is nullable.
     *
     * @return {@code true} if nullable
     */
    boolean nullable() default true;

    /**
     * Sets configurations of the column.
     *
     * @return configurations of the column
     */
    @Deprecated
    LightConfiguration[] configuration() default {};

    /**
     * Default value of the column.
     * <ul>
     *      <li>{@link #NO_DEFAULT_VALUE} means no default value.</li>
     *      <li>{@link #DEFAULT_VALUE_NULL} means defaults null.</li>
     *      <li>Otherwise the real default value.</li>
     * </ul>
     *
     * @return default value of the column
     */
    String defaultValue() default NO_DEFAULT_VALUE;

    /**
     * Set the data type of the column.
     * Set to {@link SQLDataType#UNDEFINED} to let the program infer.
     *
     * @return data type of the column
     */
    SQLDataType dataType() default SQLDataType.UNDEFINED;

    /**
     * No default value
     */
    String NO_DEFAULT_VALUE = "[LIGHT_No-Default-Value]";

    /**
     * Default null.
     */
    String DEFAULT_VALUE_NULL = "[LIGHT_Default-Value-Null]";
}
