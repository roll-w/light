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

import space.lingu.light.Delete;
import space.lingu.light.Transaction;
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.coder.annotated.binder.AnnotatedMethodBinder;
import space.lingu.light.compile.coder.annotated.binder.DirectAutoDeleteUpdateMethodBinder;
import space.lingu.light.compile.coder.annotated.translator.AutoDeleteUpdateMethodTranslator;
import space.lingu.light.compile.coder.custom.binder.DeleteResultBinder;
import space.lingu.light.compile.coder.custom.binder.HandlerDeleteResultBinder;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.*;
import space.lingu.light.util.Pair;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author RollW
 */
public class DeleteMethodProcessor implements Processor<DeleteMethod> {
    private final ExecutableElement mExecutable;
    private final TypeElement mContaining;
    private final ProcessEnv mEnv;

    public DeleteMethodProcessor(ExecutableElement element,
                                 TypeElement containing,
                                 ProcessEnv env) {
        mExecutable = element;
        mContaining = containing;
        mEnv = env;
    }

    @Override
    public DeleteMethod process() {
        DeleteMethod method = new DeleteMethod();
        AnnotateMethodProcessor delegate = new AnnotateMethodProcessor(mExecutable, mEnv);

        DaoProcessor.sHandleAnnotations.forEach(anno -> {
            if (anno != Delete.class && mExecutable.getAnnotation(anno) != null) {
                mEnv.getLog().error(
                        CompileErrors.DUPLICATED_METHOD_ANNOTATION,
                        mExecutable
                );
            }
        });
        Delete anno = mExecutable.getAnnotation(Delete.class);
        method.setElement(mExecutable)
                .setReturnType(mExecutable.getReturnType());
        boolean isAutoGenerate = anno.value().equals(Delete.AUTO_GENERATION);
        if (!isAutoGenerate) {
            if (anno.value().isEmpty()) {
                mEnv.getLog().error(
                        CompileErrors.SQL_CANNOT_BE_EMPTY,
                        mExecutable
                );
            }

            List<SQLCustomParameter> deleteParameters = new ArrayList<>();
            mExecutable.getParameters().forEach(variableElement -> {
                Processor<DeleteParameter> deleteParameterProcessor =
                        new DeleteParameterProcessor(variableElement, mContaining, mEnv);
                deleteParameters.add(deleteParameterProcessor.process());
            });
            method.setSql(anno.value())
                    .setParameters(deleteParameters);
        } else {
            Pair<Map<String, ParamEntity>, List<Parameter>> pair =
                    delegate.extractParameters(mContaining);
            method.setEntities(pair.first)
                    .setParameters(toDeleteParameters(pair.second));
        }

        AutoDeleteUpdateMethodTranslator translator =
                AutoDeleteUpdateMethodTranslator.create(
                        method.getReturnType(),
                        toParameters(method.getParameters())
                );
        if (translator == null) {
            mEnv.getLog().error(CompileErrors.DELETE_INVALID_RETURN, mExecutable);
        }

        if (isAutoGenerate) {
            AnnotatedMethodBinder methodBinder = new DirectAutoDeleteUpdateMethodBinder(translator);
            return method
                    .setBinder(methodBinder)
                    .setTransaction(mExecutable.getAnnotation(Transaction.class) != null)
                    .setResultBinder(
                            new HandlerDeleteResultBinder(method, methodBinder));
        } else {
            Processor<List<ExpressionBind>>
                    processor = new SQLBindProcessor(mExecutable, method.getSql(), mEnv);
            return method
                    .setExpressionBinds(processor.process())
                    .setResultBinder(DeleteResultBinder.getInstance());
        }
    }

    private List<Parameter> toParameters(List<SQLCustomParameter> sqlCustomParameters) {
        return new ArrayList<>(sqlCustomParameters);
    }

    private List<SQLCustomParameter> toDeleteParameters(List<Parameter> parameters) {
        List<SQLCustomParameter> sqlCustomParameters = new ArrayList<>();
        parameters.forEach(parameter -> {
            DeleteParameter deleteParameter = new DeleteParameter(parameter);
            sqlCustomParameters.add(deleteParameter);
        });
        return sqlCustomParameters;
    }
}
