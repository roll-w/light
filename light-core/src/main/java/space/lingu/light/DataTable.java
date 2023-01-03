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
 * Mark as a data table.
 *
 * @author RollW
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface DataTable {
    /**
     * Table name. If leave it empty, will use class name as table name.
     *
     * @return table name
     */
    String tableName() default "";

    /**
     * Indices of the table.
     *
     * @return {@link Index} array
     */
    Index[] indices() default {};

    String description() default "";

    /**
     * Configurations.
     * <p>
     * It will pass all configurations to fields and indexes.
     * If you specify a duplicate configuration in the field or index,
     * the configuration here will be overwritten.
     *
     * @return {@link LightConfiguration}
     */
    LightConfiguration[] configuration() default {};
}
