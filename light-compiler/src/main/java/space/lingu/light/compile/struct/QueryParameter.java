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

import space.lingu.light.compile.coder.query.binder.QueryParameterBinder;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Objects;

/**
 * 查询参数
 * @author RollW
 */
public class QueryParameter implements Parameter {
    private VariableElement element;
    private String name;
    private String sqlName;
    private TypeElement type;
    private TypeMirror typeMirror;
    private QueryParameterBinder binder;

    public QueryParameter() {
    }

    @Override
    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public QueryParameter setTypeMirror(TypeMirror typeMirror) {
        this.typeMirror = typeMirror;
        return this;
    }

    public VariableElement getElement() {
        return element;
    }

    public QueryParameter setElement(VariableElement element) {
        this.element = element;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    public QueryParameter setName(String name) {
        this.name = name;
        return this;
    }

    public String getSqlName() {
        return sqlName;
    }

    public QueryParameter setSqlName(String sqlName) {
        this.sqlName = sqlName;
        return this;
    }

    @Override
    public TypeElement getType() {
        return type;
    }

    public QueryParameter setType(TypeElement type) {
        this.type = type;
        return this;
    }

    public QueryParameterBinder getBinder() {
        return binder;
    }

    public QueryParameter setBinder(QueryParameterBinder binder) {
        this.binder = binder;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryParameter that = (QueryParameter) o;
        return Objects.equals(element, that.element) && Objects.equals(name, that.name) && Objects.equals(sqlName, that.sqlName) && Objects.equals(type, that.type) && Objects.equals(typeMirror, that.typeMirror) && Objects.equals(binder, that.binder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element, name, sqlName, type, typeMirror, binder);
    }
}
