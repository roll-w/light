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

/**
 * 数据库连接配置
 * @author Kai
 */
public class DatasourceConfig {
    private final String url;
    private final String username;// optional
    private final String password;// optional
    private final String modifier;

    public DatasourceConfig(String url, String username,
                            String password, String modifier) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.modifier = modifier;
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

    public String getModifier() {
        return modifier;
    }

    @Override
    public String toString() {
        return "DataSourceConfig{" +
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", modifier='" + modifier + '\'' +
                '}';
    }
}
