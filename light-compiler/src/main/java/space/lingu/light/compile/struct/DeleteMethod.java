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

import space.lingu.light.compile.coder.annotated.binder.AnnotatedMethodBinder;
import space.lingu.light.compile.coder.custom.binder.QueryResultBinder;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;

/**
 * @author RollW
 */
public class DeleteMethod implements SQLCustomMethod, AnnotatedMethod<SQLCustomParameter> {
    private ExecutableElement element;
    private Map<String, ParamEntity> entities;
    private TypeMirror returnType;
    private String sql = null;

    private boolean isTransaction;
    private List<SQLCustomParameter> parameters;
    private QueryResultBinder resultBinder;
    private AnnotatedMethodBinder binder;
    private List<ExpressionBind> expressionBinds;

    public DeleteMethod() {
    }

    @Override
    public ExecutableElement getElement() {
        return element;
    }

    public DeleteMethod setElement(ExecutableElement element) {
        this.element = element;
        return this;
    }

    public Map<String, ParamEntity> getEntities() {
        return entities;
    }

    public DeleteMethod setBinder(AnnotatedMethodBinder binder) {
        this.binder = binder;
        return this;
    }

    @Override
    public AnnotatedMethodBinder getBinder() {
        return binder;
    }


    public DeleteMethod setEntities(Map<String, ParamEntity> entities) {
        this.entities = entities;
        return this;
    }

    @Override
    public TypeMirror getReturnType() {
        return returnType;
    }

    public DeleteMethod setReturnType(TypeMirror returnType) {
        this.returnType = returnType;
        return this;
    }

    @Override
    public List<SQLCustomParameter> getParameters() {
        return parameters;
    }

    public DeleteMethod setParameters(List<SQLCustomParameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    @Override
    public QueryResultBinder getResultBinder() {
        return resultBinder;
    }

    @Override
    public boolean isTransaction() {
        if (sql == null) {
            return false;
        }
        return isTransaction;
    }

    public DeleteMethod setTransaction(boolean transaction) {
        isTransaction = transaction;
        return this;
    }

    public DeleteMethod setResultBinder(QueryResultBinder binder) {
        this.resultBinder = binder;
        return this;
    }

    public String getSql() {
        return sql;
    }

    public DeleteMethod setSql(String sql) {
        this.sql = sql;
        return this;
    }

    @Override
    public List<ExpressionBind> getExpressionBinds() {
        return expressionBinds;
    }

    public DeleteMethod setExpressionBinds(List<ExpressionBind> expressionBinds) {
        this.expressionBinds = expressionBinds;
        return this;
    }
}
