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

import space.lingu.light.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringJoiner;

/**
 * @author RollW
 */
public class DatasourceLoader {
    public static final String DEFAULT_PATH = "/light.properties";
    private final String mPath;
    private final String mName;

    public DatasourceLoader() {
        this(DEFAULT_PATH);
    }

    public DatasourceLoader(String path) {
        this(path, PropertiesKeys.DEFAULT_NAME);
    }

    public DatasourceLoader(String path, String name) {
        this.mPath = path;
        this.mName = name;
    }

    public DatasourceConfig load() {
        Properties properties = new Properties();
        InputStream propInput = Light.loadResource(mPath);
        if (propInput == null) {
            throw new LightRuntimeException("Load data properties failed. Check whether the Properties file exists. " +
                    "If you enter a custom path, check whether the path is correct.");
        }

        try {
            properties.load(propInput);
        } catch (IOException e) {
            throw new LightRuntimeException("Load data properties failed", e);
        }

        Enumeration<?> enumeration = properties.propertyNames();
        boolean containsName = false, containsOther = false,
                containsDefault = false;
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement().toString();
            if (PropertiesKeys.checkKey(key)) {
                throw new IllegalPropertiesException(
                        "Property key contains unusable placeholders '$N' in " + key,
                        mPath);
            }
            if (PropertiesKeys.isPropertyKey(key, mName)) {
                containsName = true;
            }
            if (PropertiesKeys.isPropertyKey(key, PropertiesKeys.DEFAULT_NAME)) {
                containsDefault = true;
            }
            if (PropertiesKeys.isNotAnyPropertyKey(key, mName)) {
                containsOther = true;
            }
        }
        if (mName.equals(PropertiesKeys.DEFAULT_NAME) &&
                containsName && containsDefault) {
            throw new IllegalPropertiesException(
                    LightErrors.errorConfigDefaultNameWithExist(mName),
                    mPath);
        }
        if (containsOther && containsDefault) {
            throw new IllegalPropertiesException(
                    LightErrors.CONFIG_DEFAULT_NAME_WITH_CONTAINS_OTHER,
                    mPath);
        }

        if (containsName) {
            return readByName(properties, mName);
        }
        return readByName(properties, PropertiesKeys.DEFAULT_NAME);
    }

    private String listEmptyPropertiesKeys(String url, String jdbcName) {
        StringJoiner joiner = new StringJoiner(",\n");
        if (StringUtil.isEmpty(url)) {
            joiner.add(PropertiesKeys.getActualUrlKey(mName));
        }
        if (StringUtil.isEmpty(jdbcName)) {
            joiner.add(PropertiesKeys.getActualJdbcNameKey(mName));
        }
        return joiner.toString();
    }

    private DatasourceConfig readByName(Properties properties, String name) {
        final String url = properties.getProperty(PropertiesKeys.getActualUrlKey(name));
        final String jdbcName = properties.getProperty(PropertiesKeys.getActualJdbcNameKey(name));
        final String username = properties.getProperty(PropertiesKeys.getActualUsernameKey(name));
        final String password = properties.getProperty(PropertiesKeys.getActualPasswordKey(name));

        if (StringUtil.isEmpty(url) || StringUtil.isEmpty(jdbcName)) {
            throw new IllegalPropertiesException(LightErrors.errorConfigRequiredKeyEmpty(
                    listEmptyPropertiesKeys(url, jdbcName)),
                    mPath);
        }

        return new DatasourceConfig(url, jdbcName, username, password);
    }

}
