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

import java.util.*;

/**
 * Configurations wrapper.
 *
 * @author RollW
 */
public final class Configurations {
    private final Configuration[] configurations;

    public Configurations(Configuration[] configurations) {
        this.configurations = configurations;
    }

    public Configuration findConfiguration(String key) {
        for (Configuration configuration : configurations) {
            if (configuration != null && Objects.equals(configuration.key, key)) {
                return configuration;
            }
        }
        return null;
    }

    public String findConfigurationValue(String key, String defaultValue) {
        for (Configuration configuration : configurations) {
            if (configuration != null && Objects.equals(configuration.key, key)) {
                return configuration.value;
            }
        }
        return defaultValue;
    }

    public String findConfigurationValue(String key) {
        return findConfigurationValue(key, null);
    }

    /**
     * Plus to another.
     * If there are duplicates, the other one is preferred.
     *
     * @param other other one
     * @return after adding
     */
    public Configurations plus(Configurations other) {
        Map<String, Configuration> map = new HashMap<>();
        for (Configuration configuration : this.configurations) {
            map.put(configuration.key, configuration);
        }
        for (Configuration configuration : other.configurations) {
            map.put(configuration.key, configuration);
        }
        Configuration[] array =
                map.values().toArray(new Configuration[0]);
        return new Configurations(array);
    }

    public Configuration[] configurations() {
        return configurations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configurations that = (Configurations) o;
        return Arrays.equals(configurations, that.configurations);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(configurations);
    }

    public static Configurations createFrom(List<Configuration> configurations) {
        if (configurations == null || configurations.isEmpty()) {
            return empty();
        }
        return new Configurations(configurations.toArray(new Configuration[0]));
    }

    public static Configurations createFrom(Configuration... configurations) {
        if (configurations == null || configurations.length == 0) {
            return empty();
        }
        return new Configurations(configurations);
    }

    public static Configurations createFrom() {
        return empty();
    }

    public static Configurations createFrom(LightConfiguration[] configurations) {
        if (configurations == null || configurations.length == 0) {
            return empty();
        }
        Configuration[] confs = new Configuration[configurations.length];
        for (int i = 0; i < configurations.length; i++) {
            confs[i] = new Configuration(
                    configurations[i].key(),
                    configurations[i].value());
        }
        return new Configurations(confs);
    }

    static final Configurations EMPTY = new Configurations(new Configuration[0]);

    public static Configurations empty() {
        return EMPTY;
    }

    /**
     * Configuration. For Light module use.
     *
     * @author RollW
     */
    public static final class Configuration {
        public final String key;
        public final String value;

        public Configuration(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public int intValue() {
            return Integer.parseInt(value);
        }

        public boolean booleanValue() {
            return Boolean.parseBoolean(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Configuration that = (Configuration) o;
            return Objects.equals(key, that.key) && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
    }
}
