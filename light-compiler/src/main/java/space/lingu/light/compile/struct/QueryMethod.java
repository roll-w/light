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

import space.lingu.light.compile.coder.custom.binder.QueryResultBinder;
import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.TypeCompileType;

import java.util.List;

/**
 * 查询方法
 * @author RollW
 */
public class QueryMethod implements SQLCustomMethod {
    private final MethodCompileType methodCompileType;
    private final String sql;
    private final QueryResultBinder resultBinder;
    private final List<SQLCustomParameter> parameters;
    private final List<ExpressionBind> expressionBinds;
    private final boolean transaction;

    public QueryMethod(MethodCompileType methodCompileType, String sql,
                       QueryResultBinder resultBinder,
                       List<SQLCustomParameter> parameters,
                       List<ExpressionBind> expressionBinds,
                       boolean transaction) {
        this.methodCompileType = methodCompileType;
        this.sql = sql;
        this.resultBinder = resultBinder;
        this.parameters = parameters;
        this.expressionBinds = expressionBinds;
        this.transaction = transaction;
    }

    @Override
    public MethodCompileType getMethodCompileType() {
        return methodCompileType;
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public QueryResultBinder getResultBinder() {
        return resultBinder;
    }

    @Override
    public List<SQLCustomParameter> getParameters() {
        return parameters;
    }

    @Override
    public TypeCompileType getReturnType() {
        return methodCompileType.getReturnType();
    }

    @Override
    public List<ExpressionBind> getExpressionBinds() {
        return expressionBinds;
    }

    @Override
    public boolean isTransaction() {
        return transaction;
    }
}
