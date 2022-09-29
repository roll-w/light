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

import java.util.Objects;

/**
 * Database connection configuration.
 *
 * @author RollW
 */
public class DatasourceConfig {
    private final String url;
    private final String jdbcName;
    private final String username;// optional
    private final String password;// optional

    public DatasourceConfig(String url, String jdbcName, String username,
                            String password) {
        this.url = url;
        this.jdbcName = jdbcName;
        this.username = username;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getJdbcName() {
        return jdbcName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasourceConfig config = (DatasourceConfig) o;
        return Objects.equals(url, config.url) &&
                Objects.equals(jdbcName, config.jdbcName) &&
                Objects.equals(username, config.username) &&
                Objects.equals(password, config.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, jdbcName, username, password);
    }

    @Override
    public String toString() {
        return "DataSourceConfig{" +
                "url='" + url + '\'' +
                "jdbcName='" + jdbcName + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
