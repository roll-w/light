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

import java.util.Collections;
import java.util.List;

/**
 * @author RollW
 */
public class Table {
    /**
     * Table name
     */
    private final String name;

    /**
     * Table columns
     */
    private final List<TableColumn> columns;

    private final TablePrimaryKey primaryKey;

    /**
     * Table indices
     */
    private final List<TableIndex> indices;
    // private final List<ForeignKey> foreignKeys;

    public Table(String name, List<TableColumn> columns,
                 TablePrimaryKey primaryKey, List<TableIndex> indices) {
        this.name = name;
        this.columns = Collections.unmodifiableList(columns);
        this.primaryKey = primaryKey;
        this.indices = Collections.unmodifiableList(indices);
    }


    public String getName() {
        return name;
    }

    public List<TableColumn> getColumns() {
        return columns;
    }

    public List<TableIndex> getIndices() {
        return indices;
    }

    public TablePrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", columns=" + columns +
                ", indices=" + indices +
                '}';
    }
}
