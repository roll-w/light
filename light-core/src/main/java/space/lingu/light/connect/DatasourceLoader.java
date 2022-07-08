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

package space.lingu.light.connect;

import space.lingu.light.Light;
import space.lingu.light.LightRuntimeException;
import space.lingu.light.constant.PropertiesKeys;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author RollW
 */
public class DatasourceLoader {
    private static final String DEFAULT_PATH = "/light.properties";
    private final String path;

    public DatasourceLoader() {
        this(DEFAULT_PATH);
    }

    public DatasourceLoader(String path) {
        this.path = path;
    }

    public DatasourceConfig load() {
        Properties properties = new Properties();
        InputStream propInput = Light.loadResource(path);
        try {
            properties.load(propInput);
        } catch (IOException e) {
            throw new LightRuntimeException("Load data properties failed. Check whether the Properties file exists. " +
                    "If you enter a custom path, check whether the path is correct.");
        }
        final String url = properties.getProperty(PropertiesKeys.URL_KEY);
        final String jdbcName = properties.getProperty(PropertiesKeys.JDBC_NAME_KEY);
        final String username = properties.getProperty(PropertiesKeys.USERNAME_KEY, null);
        final String password = properties.getProperty(PropertiesKeys.PASSWORD_KEY, null);
        final String modifier = properties.getProperty(PropertiesKeys.MODIFIER_KEY, null);
        return new DatasourceConfig(url, jdbcName, username, password, modifier);

        // TODO: 多数据配置读取
    }

}
