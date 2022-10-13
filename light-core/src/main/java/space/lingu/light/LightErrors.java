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
 * Define error messages.
 *
 * @author RollW
 */
public final class LightErrors {
    private static final String CONFIG_DEFAULT_NAME_WITH_EXIST =
            "It's unknown how to handle duplicate properties. " +
                    "Cannot use default name 'data' with exists properties with name '%s'";

    public static String errorConfigDefaultNameWithExist(String name) {
        return String.format(CONFIG_DEFAULT_NAME_WITH_EXIST, name);
    }

    public static final String CONFIG_DEFAULT_NAME_WITH_CONTAINS_OTHER =
            "Cannot use the default name 'data' in a properties that contains other's properties.";

    private static final String CONFIG_REQUIRED_KEY_EMPTY =
            "The JdbcName and Url properties cannot be empty. Please check properties list in your file: \n%s";

    public static String errorConfigRequiredKeyEmpty(String emptyKeys) {
        return String.format(CONFIG_REQUIRED_KEY_EMPTY, emptyKeys);
    }

    private LightErrors() {
    }
}
