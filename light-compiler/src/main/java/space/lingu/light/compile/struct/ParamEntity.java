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

package space.lingu.light.compile.struct;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RollW
 */
public class ParamEntity {
    private final DataTable dataTable;
    private final Pojo pojo;

    private final String tableName;

    public ParamEntity(DataTable table, Pojo pojo) {
        this.dataTable = table;
        this.pojo = pojo;
        this.tableName = dataTable.getTableName();
    }

    public Pojo getPojo() {
        if (pojo == null) {
            return dataTable;
        }
        return pojo;
    }

    public boolean isPartialEntity() {
        return pojo != null;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public String getTableName() {
        return tableName;
    }

    public PrimaryKey getPrimaryKey() {
        if (pojo == null) {
            return dataTable.getPrimaryKey();
        }
        List<Field> primaryKeyFields = new ArrayList<>();
        dataTable.getPrimaryKey().getFields().getFields().forEach(field ->
                primaryKeyFields.add(pojo.findFieldByColumnName(field.getColumnName())));
        return new PrimaryKey(dataTable.getPrimaryKey().getDeclaredIn(),
                new Field.Fields(primaryKeyFields),
                dataTable.getPrimaryKey().isAutoGenerate());
    }
}
