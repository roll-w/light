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

package space.lingu.light.compile.processor;

import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.coder.custom.binder.QueryParameterBinder;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.parser.SQLParser;
import space.lingu.light.compile.struct.ExpressionBind;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/**
 * @author RollW
 */
public class SQLBindProcessor implements Processor<List<ExpressionBind>> {
    private final ExecutableElement mElement;
    private final String mSql;
    private final ProcessEnv mEnv;


    public SQLBindProcessor(ExecutableElement element,
                            String sql,
                            ProcessEnv env) {
        mElement = element;
        mSql = sql;
        mEnv = env;
    }

    @Override
    public List<ExpressionBind> process() {
        SQLParser parser = new SQLParser(mSql, mElement);
        List<String> expressions = parser.expressions();
        List<ExpressionBind> binds = new ArrayList<>();
        expressions.forEach(expression -> {
            TypeMirror mirror = parser.findType(expression);
            if (mirror == null) {
                mEnv.getLog().error(
                        CompileErrors.QUERY_UNKNOWN_PARAM + " In [expression] of " + expression,
                        mElement
                );
            }
            QueryParameterBinder binder = mEnv.getBinders().findQueryParameterBinder(mirror);
            if (binder == null) {
                mEnv.getLog().error(
                        CompileErrors.QUERY_UNKNOWN_PARAM + " In [type] of " + expression,
                        mElement
                );
            }
            ExpressionBind bind = new ExpressionBind(expression, mirror, binder);
            binds.add(bind);
        });
        return binds;
    }
}
