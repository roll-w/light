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

package space.lingu.light.compile.struct;

/**
 * Built-in parseable data types
 *
 * @author RollW
 */
public enum ParseDataType {
    INT, LONG, SHORT,
    FLOAT, DOUBLE,
    BYTE, CHAR, BOOLEAN,
    STRING, ENUM;

    public static ParseDataType getDataType(Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return INT;
        } else if (type == long.class || type == Long.class) {
            return LONG;
        } else if (type == short.class || type == Short.class) {
            return SHORT;
        } else if (type == boolean.class || type == Boolean.class) {
            return BOOLEAN;
        } else if (type == double.class || type == Double.class) {
            return DOUBLE;
        } else if (type == float.class || type == Float.class) {
            return FLOAT;
        } else if (type == char.class || type == Character.class) {
            return CHAR;
        } else if (type == byte.class || type == Byte.class) {
            return BYTE;
        } else if (type == String.class) {
            return STRING;
        } else if (type.isEnum()) {
            return ENUM;
        }
        return null;
    }

    public static boolean isBasicType(Class<?> type) {
        return null != getDataType(type);
    }

    public static boolean isBasicType(Object obj) {
        if (obj == null) {
            return false;
        }
        return null != getDataType(obj.getClass());
    }
}
