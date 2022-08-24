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

package space.lingu.light.struct;

import space.lingu.light.SQLDataType;

/**
 * @author RollW
 */
public class TableColumn {
    /**其在数据表中的名称(默认与fieldName相同)*/
    private final String name;
    private final String fieldName;
    private final String defaultValue;
    private final SQLDataType dataType;

    public TableColumn(String name,
                       String fieldName,
                       String defaultValue,
                       SQLDataType dataType) {
        this.name = name;
        this.fieldName = fieldName;
        this.defaultValue = defaultValue;
        this.dataType = dataType;
    }

    public SQLDataType getDataType() {
        return dataType;
    }

    public String getName() {
        return name;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return "TableColumn{" +
                "name='" + name + '\'' +
                ", fieldName='" + fieldName + '\'' +
                '}';
    }
}
