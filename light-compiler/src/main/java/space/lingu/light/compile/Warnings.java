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

package space.lingu.light.compile;

import space.lingu.light.LightIgnore;
import space.lingu.light.compile.javac.CompileType;

import javax.lang.model.AnnotatedConstruct;
import java.text.MessageFormat;

/**
 * @author RollW
 */
public final class Warnings {
    private static final String DEPRECATED =
            "\n\nYou are using a deprecated API: {0}, please replace it in time.";
    private static final String INFO =
            "\n\nThis warning is intended to allow you to recheck the correctness of your annotations. " +
                    "To ignore this warning permanently, use the @LightIgnore annotation with key: \"{0}\".";

    private static String getInfoOf(String key) {
        return MessageFormat.format(INFO, key);
    }

    private static String getDeprecatedOf(String name) {
        return MessageFormat.format(DEPRECATED, name);
    }

    private static final String FIELD_NOT_ANNOTATED_KEY = "FieldNotAnnotated";
    private static final String FIELD_NOT_ANNOTATED_VALUE =
            "There is a field not covered by @DataColumn annotation: \"{0}\"." +
                    getInfoOf(FIELD_NOT_ANNOTATED_KEY);

    public static final Warning FIELD_NOT_ANNOTATED =
            new Warning(FIELD_NOT_ANNOTATED_KEY, FIELD_NOT_ANNOTATED_VALUE);

    private static final String PRIMARY_KEY_NOT_FOUND_KEY =
            "PrimaryKeyNotFound";
    private static final String PRIMARY_KEY_NOT_FOUND_VALUE =
            "Not set a primary key in the @DataTable class: \"{0}\"." + getInfoOf(PRIMARY_KEY_NOT_FOUND_KEY);
    public static final Warning PRIMARY_KEY_NOT_FOUND =
            new Warning(PRIMARY_KEY_NOT_FOUND_KEY, PRIMARY_KEY_NOT_FOUND_VALUE);

    private static final String CANNOT_APPLY_TO_STATIC_FIELD_KEY =
            "CannotApplyToStaticField";
    private static final String CANNOT_APPLY_TO_STATIC_FIELD_VALUE =
            "Cannot apply the @DataColumn to a static field: \"{0}\", this field will be ignored." +
                    getInfoOf(CANNOT_APPLY_TO_STATIC_FIELD_KEY);
    public static final Warning CANNOT_APPLY_TO_STATIC_FIELD =
            new Warning(CANNOT_APPLY_TO_STATIC_FIELD_KEY, CANNOT_APPLY_TO_STATIC_FIELD_VALUE);


    private static final String PRIMARY_KEYS_DEPRECATED_KEY =
            "PrimaryKeysDeprecated";
    private static final String PRIMARY_KEYS_DEPRECATED_VALUE =
            "You are still using primaryKeys() to define the primary key." +
                    " This will be removed in the future." +
                    getDeprecatedOf("primaryKey()") +
                    getInfoOf(PRIMARY_KEYS_DEPRECATED_KEY);
    public static final Warning PRIMARY_KEYS_DEPRECATED =
            new Warning(PRIMARY_KEYS_DEPRECATED_KEY, PRIMARY_KEYS_DEPRECATED_VALUE);

    public static boolean isNotIgnored(Warning warning,
                                       CompileType compileType) {
        return !isIgnored(warning, compileType);
    }

    public static boolean isNotIgnored(Warning warning,
                                       AnnotatedConstruct annotatedConstruct) {
        return !isIgnored(warning, annotatedConstruct);
    }

    public static boolean isIgnored(Warning warning,
                                    CompileType compileType) {
        return checkIgnore(compileType.getAnnotation(LightIgnore.class),
                warning);
    }

    public static boolean isIgnored(Warning warning,
                                    AnnotatedConstruct annotatedConstruct) {
        return checkIgnore(annotatedConstruct.getAnnotation(LightIgnore.class),
                warning);
    }

    private static boolean checkIgnore(LightIgnore ignore, Warning warning) {
        if (ignore == null) {
            return false;
        }
        String[] ignoredKeys = ignore.value();
        for (String ignoredKey : ignoredKeys) {
            if (ignoredKey.equals(warning.getKey())) {
                return true;
            }
        }
        return false;
    }

    public static class Warning {
        private final String key;
        private final String value;

        public Warning(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getValue(Object... args) {
            return MessageFormat.format(value, args);
        }
    }

    private Warnings() {
    }

}
