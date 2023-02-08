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

import com.squareup.javapoet.TypeName;
import space.lingu.light.Configurations;
import space.lingu.light.compile.javac.TypeCompileType;

import java.util.List;

/**
 * DataTable
 * @author RollW
 */
public class DataTable extends Pojo implements Configurable {
    private final String tableName;
    private final PrimaryKey primaryKey;
    private final List<Index> indices;
    private final List<ForeignKey> foreignKeys;
    private final Configurations configurations;

    public DataTable(TypeCompileType typeCompileType,
                     Field.Fields fields, Constructor constructor,
                     String tableName, PrimaryKey primaryKey,
                     List<Index> indices, List<ForeignKey> foreignKeys,
                     Configurations configurations) {
        super(typeCompileType, fields, constructor);
        this.tableName = tableName;
        this.primaryKey = primaryKey;
        this.indices = indices;
        this.foreignKeys = foreignKeys;
        this.configurations = configurations;
    }


    public String getTableName() {
        return tableName;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public List<Index> getIndices() {
        return indices;
    }

    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    @Override
    public TypeName getTypeName() {
        return super.getTypeName();
    }

    @Override
    public Constructor getConstructor() {
        return super.getConstructor();
    }

    @Override
    public Configurations getConfigurations() {
        if (configurations == null) {
            return Configurations.empty();
        }
        return configurations;
    }
}
