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
import java.util.Objects;

/**
 * @author RollW
 */
public class TablePrimaryKey {
    private final List<TableColumn> columns;
    private final boolean autoGenerate;

    public TablePrimaryKey(List<TableColumn> columns, boolean autoGenerate) {
        this.columns = columns == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(columns);
        this.autoGenerate = autoGenerate;
    }

    public boolean containsColumn(TableColumn column) {
        for (TableColumn tableColumn : columns) {
            if (Objects.equals(tableColumn.getName(), column.getName())) {
                return true;
            }
        }
        return false;
    }

    public List<TableColumn> getColumns() {
        return columns;
    }

    public boolean isAutoGenerate() {
        return autoGenerate;
    }

    public boolean isComposePrimary() {
        if (columns.isEmpty()) {
            return false;
        }
        return columns.size() != 1;
    }
}

