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

import space.lingu.light.Configurations;
import space.lingu.light.SQLDataType;

/**
 * Represents a column extracts from {@link space.lingu.light.DataColumn}
 *
 * @author RollW
 */
public class TableColumn {

    /**
     * Its name in the Table.
     */
    private final String name;

    /**
     * Field name.
     */
    private final String fieldName;

    /**
     * Default value.
     * <p>
     * It only makes sense when {@link #hasDefaultValue} is {@code true}.
     */
    private final String defaultValue;

    /**
     * Has default value or not.
     */
    private final boolean hasDefaultValue;
    private final SQLDataType dataType;
    private final boolean nullable;
    private final boolean autoGenerate;
    private final Configurations configurations;

    public TableColumn(String name, String fieldName,
                       String defaultValue,
                       boolean hasDefaultValue,
                       SQLDataType dataType,
                       boolean nullable,
                       boolean autoGenerate,
                       Configurations configurations) {
        this.name = name;
        this.fieldName = fieldName;
        this.defaultValue = defaultValue;
        this.hasDefaultValue = hasDefaultValue;
        this.dataType = dataType;
        this.nullable = nullable;
        this.autoGenerate = autoGenerate;
        this.configurations = configurations;
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

    public boolean isHasDefaultValue() {
        return hasDefaultValue;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isAutoGenerate() {
        return autoGenerate;
    }

    public Configurations getConfigurations() {
        return configurations;
    }

    @Override
    public String toString() {
        return "TableColumn{" +
                "name='" + name + '\'' +
                ", fieldName='" + fieldName + '\'' +
                '}';
    }
}
