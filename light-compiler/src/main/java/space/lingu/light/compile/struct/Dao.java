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
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.TypeElement;
import java.util.List;

/**
 * DAO
 * @author RollW
 */
public class Dao {
    private TypeElement element;
    private ClassName className;
    private String simpleName;

    private List<InsertMethod> insertMethods;
    private List<UpdateMethod> updateMethods;
    private List<DeleteMethod> deleteMethods;
    private List<QueryMethod> queryMethods;
    private List<TransactionMethod> transactionMethods;

    private ClassName implClassName;
    private String implName;

    public ClassName getClassName() {
        return className;
    }

    public Dao setClassName(ClassName className) {
        this.className = className;
        return this;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public Dao setSimpleName(String simpleName) {
        this.simpleName = simpleName;
        return this;
    }

    public ClassName getImplClassName() {
        return implClassName;
    }

    public Dao setImplClassName(ClassName implClassName) {
        this.implClassName = implClassName;
        return this;
    }

    public List<TransactionMethod> getTransactionMethods() {
        return transactionMethods;
    }

    public Dao setTransactionMethods(List<TransactionMethod> transactionMethods) {
        this.transactionMethods = transactionMethods;
        return this;
    }

    public TypeElement getElement() {
        return element;
    }

    public Dao setElement(TypeElement element) {
        this.element = element;
        return setClassName(ClassName.get(element));
    }

    public List<InsertMethod> getInsertMethods() {
        return insertMethods;
    }

    public Dao setInsertMethods(List<InsertMethod> insertMethods) {
        this.insertMethods = insertMethods;
        return this;
    }

    public List<UpdateMethod> getUpdateMethods() {
        return updateMethods;
    }

    public Dao setUpdateMethods(List<UpdateMethod> updateMethods) {
        this.updateMethods = updateMethods;
        return this;
    }

    public List<DeleteMethod> getDeleteMethods() {
        return deleteMethods;
    }

    public Dao setDeleteMethods(List<DeleteMethod> deleteMethods) {
        this.deleteMethods = deleteMethods;
        return this;
    }

    public List<QueryMethod> getQueryMethods() {
        return queryMethods;
    }

    public Dao setQueryMethods(List<QueryMethod> queryMethods) {
        this.queryMethods = queryMethods;
        return this;
    }

    public String getImplName() {
        return implName;
    }

    public Dao setImplName(String implName) {
        this.implName = implName;
        return this;
    }
}
