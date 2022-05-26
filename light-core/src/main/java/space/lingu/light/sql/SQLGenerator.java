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

package space.lingu.light.sql;

import space.lingu.light.OnConflictStrategy;

/**
 * 生成SQL语句
 * @author RollW
 */
public interface SQLGenerator {
    String BACK_QUOTE = "`";

    // TODO: 语句生成接口
    String generateInsert(String tableName, String... valueArgs);

    String generateInsert(String tableName, OnConflictStrategy onConflict, String... valueArgs);

    String generateDelete(String tableName, String... conditions);

    String generateDelete(String tableName, String conditionName, int args);

    String generateUpdate(String tableName, String... valueArgs);

    String placeHolders(int argsCount);

    String escapeParam(String param);
}
