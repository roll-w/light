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
 * Annotated as a database.
 *
 * @author RollW
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface Database {
    /**
     * Database name.
     * <p>
     * Light doesn't check for duplicate database names,
     * so that requires your own attention.
     * <p>
     * The naming conventions must follow the conventions of
     * the database used.
     *
     * @return Database name
     */
    String name();

    /**
     * Database connection configuration read path.
     * The file needs to be a properties file.
     *
     * @return Database connection configuration read path
     */
    String datasourceConfig() default DatasourceLoader.DEFAULT_PATH;

    int version();

    Class<?>[] tables();

    /**
     * Database configurations.
     *
     * @return Database configurations
     * @deprecated use {@link  LightConfiguration} directly annotate on class to instead
     */
    @Deprecated
    LightConfiguration[] configuration() default {};
}
