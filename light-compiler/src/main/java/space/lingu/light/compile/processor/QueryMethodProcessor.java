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
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.QueryMethod;
import space.lingu.light.compile.struct.QueryParameter;
import space.lingu.light.compile.struct.SQLCustomParameter;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询方法处理器
 * @author RollW
 */
public class QueryMethodProcessor implements Processor<QueryMethod> {
    private final ExecutableElement mExecutable;
    private final TypeElement mContaining;
    private final ProcessEnv mEnv;
    private final QueryMethod method = new QueryMethod();

    public QueryMethodProcessor(ExecutableElement element,
                                TypeElement containing,
                                ProcessEnv env) {
        mExecutable = element;
        mContaining = containing;
        mEnv = env;
    }

    @Override
    public QueryMethod process() {
        Query queryAnno = mExecutable.getAnnotation(Query.class);
        if (queryAnno.value().isEmpty()) {
            throw new LightCompileException("Query method value cannot be empty, must be a sql sentence.");
        }
        DaoProcessor.sHandleAnnotations.forEach(anno -> {
            if (anno != Query.class && mExecutable.getAnnotation(anno) != null) {
                mEnv.getLog().error(
                        CompileErrors.DUPLICATED_METHOD_ANNOTATION,
                        mExecutable
                );
            }
        });

        List<? extends VariableElement> parameters = mExecutable.getParameters();
        List<SQLCustomParameter> queryParameters = new ArrayList<>();
        parameters.forEach(variableElement -> {
            Processor<QueryParameter> parameterProcessor =
                    new QueryParameterProcessor(variableElement, mContaining, mEnv);
            queryParameters.add(parameterProcessor.process());
        });

        method.setElement(mExecutable)
                .setSql(queryAnno.value())
                .setReturnType(mExecutable.getReturnType())
                .setParameters(queryParameters)
                .setTransaction(mExecutable.getAnnotation(Transaction.class) != null);
        QueryResultBinder binder =
                mEnv.getBinders().findQueryResultBinder(method.getReturnType());
        if (binder == null) {
            mEnv.getLog().error(
                    CompileErrors.QUERY_UNKNOWN_RETURN_TYPE,
                    mExecutable
            );
        }
        return method.setResultBinder(binder);
    }
}
