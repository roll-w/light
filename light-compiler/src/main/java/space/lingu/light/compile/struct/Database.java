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

import com.squareup.javapoet.ClassName;
import space.lingu.light.Configurations;

import javax.lang.model.element.TypeElement;
import java.util.List;

/**
 * Database
 *
 * @author RollW
 */
public class Database implements Configurable {
    private TypeElement superClassElement;// represents super class
    private List<DataTable> dataTableList;
    private List<DatabaseDaoMethod> databaseDaoMethods;
    private ClassName superClassName;
    private ClassName implClassName;
    private String implName;
    private Configurations configurations;

    public Database() {
    }

    public Database(TypeElement superClassElement) {
        this.superClassElement = superClassElement;
    }

    public ClassName getImplClassName() {
        return implClassName;
    }

    public Database setImplClassName(ClassName implClassName) {
        this.implClassName = implClassName;
        return this;
    }

    public String getImplName() {
        return implName;
    }

    public Database setImplName(String implName) {
        this.implName = implName;
        return this;
    }

    public ClassName getSuperClassName() {
        if (superClassName == null) {
            superClassName = ClassName.get(superClassElement);
        }
        return superClassName;
    }

    public TypeElement getSuperClassElement() {
        return superClassElement;
    }

    public Database setSuperClassElement(TypeElement superClassElement) {
        this.superClassElement = superClassElement;
        return this;
    }

    public List<DataTable> getDataTableList() {
        return dataTableList;
    }

    public Database setDataTableList(List<DataTable> entities) {
        this.dataTableList = entities;
        return this;
    }

    public List<DatabaseDaoMethod> getDatabaseDaoMethods() {
        return databaseDaoMethods;
    }

    public Database setDatabaseDaoMethods(List<DatabaseDaoMethod> databaseDaoMethods) {
        this.databaseDaoMethods = databaseDaoMethods;
        return this;
    }

    @Override
    public Configurations getConfigurations() {
        if (configurations == null) {
            return Configurations.empty();
        }
        return configurations;
    }

    public Database setConfigurations(Configurations configurations) {
        this.configurations = configurations;
        return this;
    }
}
