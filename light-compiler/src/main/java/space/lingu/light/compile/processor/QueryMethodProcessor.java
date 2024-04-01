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

import space.lingu.light.Query;
import space.lingu.light.Transaction;
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.coder.custom.binder.QueryResultBinder;
import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.javac.VariableCompileType;
import space.lingu.light.compile.struct.ExpressionBind;
import space.lingu.light.compile.struct.QueryMethod;
import space.lingu.light.compile.struct.QueryParameter;
import space.lingu.light.compile.struct.SQLCustomParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Query method processor.
 *
 * @author RollW
 */
public class QueryMethodProcessor implements Processor<QueryMethod> {
    private final MethodCompileType methodCompileType;
    private final TypeCompileType mContaining;
    private final ProcessEnv mEnv;

    public QueryMethodProcessor(MethodCompileType methodCompileType,
                                TypeCompileType containing,
                                ProcessEnv env) {
        this.methodCompileType = methodCompileType;
        mContaining = containing;
        mEnv = env;
    }

    @Override
    public QueryMethod process() {
        Query queryAnno = methodCompileType.getAnnotation(Query.class);
        if (queryAnno.value().isEmpty()) {
            mEnv.getLog().error(CompileErrors.QUERY_SQL_EMPTY, methodCompileType);
        }
        DaoProcessor.HANDLE_ANNOTATIONS.forEach(anno -> {
            if (anno != Query.class && methodCompileType.getAnnotation(anno) != null) {
                mEnv.getLog().error(
                        CompileErrors.DUPLICATED_METHOD_ANNOTATION,
                        methodCompileType
                );
            }
        });

        List<VariableCompileType> parameters = methodCompileType.getParameters();
        List<SQLCustomParameter> queryParameters = new ArrayList<>();
        parameters.forEach(variableElement -> {
            Processor<QueryParameter> parameterProcessor =
                    new QueryParameterProcessor(variableElement, mContaining, mEnv);
            queryParameters.add(parameterProcessor.process());
        });

        final String sql = queryAnno.value();

        boolean transaction = methodCompileType.getAnnotation(Transaction.class) != null;
        QueryResultBinder binder = null;
        try {
            binder = mEnv.getBinders().findQueryResultBinder(
                    methodCompileType.getReturnType());
        } catch (LightCompileException e) {
            // TODO: move unbound check here
            // e.printStackTrace();
            mEnv.getLog().error(e.getMessage(), methodCompileType);
        }

        if (binder == null) {
            mEnv.getLog().error(
                    CompileErrors.QUERY_UNKNOWN_RETURN_TYPE,
                    methodCompileType
            );
        }
        Processor<List<ExpressionBind>>
                processor = new SQLBindProcessor(methodCompileType, sql, mEnv);
        List<ExpressionBind> binds = processor.process();
        return new QueryMethod(methodCompileType,
                sql, binder,
                queryParameters,  binds,
                transaction);
    }

    private void checkUnboundType(List<SQLCustomParameter> parameters) {
        // TODO: unbound parameters check
    }
}
