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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Pass configuration information.
 * <p>
 * Configuration may not work or be ignored in some databases.
 *
 * @author RollW
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface LightConfiguration {
    String key();

    String value();

    /* Keys */

    /**
     * varchar length configuration.
     *
     * @see space.lingu.light.sql.MySQLDialectProvider#DEFAULT_VARCHAR_LENGTH
     */
    String KEY_VARCHAR_LENGTH = "Light.Key.VarcharLength";

    /**
     * charset configuration
     *
     * @see space.lingu.light.sql.MySQLDialectProvider#DEFAULT_CHARSET
     */
    String KEY_CHARSET = "Light.Key.Charset";

    /**
     * Engine configuration
     *
     * @see space.lingu.light.sql.MySQLDialectProvider#DEFAULT_ENGINE
     */
    String KEY_ENGINE = "Light.Key.Engine";

    /**
     * Custom column type.
     * <p>
     * Applies to variable-length strings to specify the length,
     * e.g.: varchar(30).
     * It is not recommended to use it on basic types such as
     * int, float, or etc. and also custom types.
     * <p>
     * It is recommended to use it on enum fields or other fields of limited length.
     * <p>
     * Compare to {@link #KEY_VARCHAR_LENGTH}, this configuration contains more fine-grained control.
     */
    String KEY_COLUMN_TYPE = "Light.Key.ColumnType";
}
