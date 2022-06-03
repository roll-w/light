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

import space.lingu.light.compile.coder.query.binder.QueryResultBinder;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * 查询方法
 * @author RollW
 */
public class QueryMethod implements Method<QueryParameter> {
    private ExecutableElement element;
    private TypeMirror returnType;
    private List<QueryParameter> parameters;
    private String query;
    private boolean isTransaction;
    private QueryResultBinder binder;

    @Override
    public ExecutableElement getElement() {
        return element;
    }

    public QueryMethod setElement(ExecutableElement element) {
        this.element = element;
        return this;
    }

    public boolean isTransaction() {
        return isTransaction;
    }

    public QueryMethod setTransaction(boolean transaction) {
        isTransaction = transaction;
        return this;
    }

    @Override
    public List<QueryParameter> getParameters() {
        return parameters;
    }

    public QueryMethod setParameters(List<QueryParameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public QueryMethod setQuery(String query) {
        this.query = query;
        return this;
    }

    @Override
    public TypeMirror getReturnType() {
        return returnType;
    }

    public QueryMethod setReturnType(TypeMirror returnTypeMirror) {
        this.returnType = returnTypeMirror;
        return this;
    }

    public QueryResultBinder getBinder() {
        return binder;
    }

    public QueryMethod setBinder(QueryResultBinder binder) {
        this.binder = binder;
        return this;
    }
}
