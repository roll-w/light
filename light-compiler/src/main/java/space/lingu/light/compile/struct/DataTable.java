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

import javax.lang.model.element.TypeElement;
import java.util.List;

/**
 * DataTable
 * @author RollW
 */
public class DataTable extends Pojo {
    private TypeElement element;
    private String tableName;
    private List<Field> fields;
    private PrimaryKey primaryKey;
    private List<Index> indices;
    private List<ForeignKey> foreignKeys;

    public DataTable() {
    }

    public TypeElement getElement() {
        return element;
    }

    public DataTable setElement(TypeElement element) {
        this.element = element;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public DataTable setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public List<Field> getFields() {
        return fields;
    }

    public DataTable setFields(List<Field> fields) {
        this.fields = fields;
        return this;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public DataTable setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    public List<Index> getIndices() {
        return indices;
    }

    public DataTable setIndices(List<Index> indices) {
        this.indices = indices;
        return this;
    }

    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public DataTable setForeignKeys(List<ForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
        return this;
    }

    @Override
    public TypeName getTypeName() {
        return super.getTypeName();
    }

    @Override
    public Pojo setTypeName(TypeName typeName) {
        return super.setTypeName(typeName);
    }

    @Override
    public Constructor getConstructor() {
        return super.getConstructor();
    }

    @Override
    public Pojo setConstructor(Constructor constructor) {
        return super.setConstructor(constructor);
    }
}
