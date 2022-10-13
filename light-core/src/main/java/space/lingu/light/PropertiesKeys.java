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
 * Keys in properties file.
 *
 * @author RollW
 */
public class PropertiesKeys {
    private static final String PLACEHOLDER_PATTERN = "\\$N";
    public static final String DEFAULT_NAME = "data";

    public static final String URL_KEY = "light.$N.url";
    public static final String JDBC_NAME_KEY = "light.$N.jdbcName";
    public static final String USERNAME_KEY = "light.$N.username";
    public static final String PASSWORD_KEY = "light.$N.password";

    public static String getActualKey(String key, String name) {
        return key.replaceAll(PLACEHOLDER_PATTERN, name);
    }

    public static String getActualUrlKey(String name) {
        return getActualKey(URL_KEY, name);
    }

    public static String getActualJdbcNameKey(String name) {
        return getActualKey(JDBC_NAME_KEY, name);
    }

    public static String getActualUsernameKey(String name) {
        return getActualKey(USERNAME_KEY, name);
    }

    public static String getActualPasswordKey(String name) {
        return getActualKey(PASSWORD_KEY, name);
    }

    public static boolean checkKey(String key) {
        if (key == null) {
            return false;
        }
        return key.startsWith("light.$N");
    }

    public static boolean isPropertyKey(String key, String name) {
        return key.startsWith("light." + name);
    }

    public static boolean isLightKey(String key) {
        return key.startsWith("light.");
    }

    public static boolean isNotAnyPropertyKey(String key, String name) {
        if (!isLightKey(key)) {
            return false;
        }
        if (isPropertyKey(key, name)) {
            return false;
        }
        return !isPropertyKey(key, DEFAULT_NAME);
    }

    private PropertiesKeys() {
    }
}
