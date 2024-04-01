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

package space.lingu.light.util;

import java.util.Locale;

public final class StringUtils {

    public static boolean isEmpty(String s) {
        if (s == null) {
            return true;
        }
        return s.isEmpty();
    }


    public static String firstUpperCase(String s) {
        if (isEmpty(s)) {
            return "";
        }
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        }
        if (s.length() == 1) {
            return s.toUpperCase(Locale.US);
        }
        return s.substring(0, 1).toUpperCase(Locale.US) + s.substring(1);
    }

    public static String firstLowerCase(String s) {
        if (isEmpty(s)) {
            return "";
        }
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        }
        if (s.length() == 1) {
            return s.toLowerCase(Locale.US);
        }
        return s.substring(0, 1).toLowerCase(Locale.US) + s.substring(1);
    }
}
