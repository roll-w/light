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

import space.lingu.light.compile.coder.custom.binder.QueryParameterBinder;
import space.lingu.light.compile.javac.TypeCompileType;

/**
 * Expression bind info
 *
 * @author RollW
 */
public class ExpressionBind {
    private final String expression;
    private final TypeCompileType type;
    private final QueryParameterBinder binder;

    public ExpressionBind(String expression,
                          TypeCompileType type,
                          QueryParameterBinder binder) {
        this.expression = expression;
        this.type = type;
        this.binder = binder;
    }

    public String getExpression() {
        return expression;
    }

    public TypeCompileType getType() {
        return type;
    }

    public QueryParameterBinder getBinder() {
        return binder;
    }
}
