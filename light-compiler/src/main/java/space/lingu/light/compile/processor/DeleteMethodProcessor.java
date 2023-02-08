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
import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.struct.DeleteMethod;
import space.lingu.light.compile.struct.DeleteParameter;
import space.lingu.light.compile.struct.ExpressionBind;
import space.lingu.light.compile.struct.ParamEntity;
import space.lingu.light.compile.struct.Parameter;
import space.lingu.light.compile.struct.SQLCustomParameter;
import space.lingu.light.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author RollW
 */
public class DeleteMethodProcessor implements Processor<DeleteMethod> {
    private final MethodCompileType methodCompileType;
    private final TypeCompileType containing;
    private final ProcessEnv mEnv;

    public DeleteMethodProcessor(MethodCompileType methodCompileType,
                                 TypeCompileType containing,
                                 ProcessEnv env) {
        this.methodCompileType = methodCompileType;
        this.containing = containing;
        mEnv = env;
    }

    @Override
    public DeleteMethod process() {
        AnnotateMethodProcessor delegate = new AnnotateMethodProcessor(methodCompileType, mEnv);
        DaoProcessor.sHandleAnnotations.forEach(anno -> {
            if (anno != Delete.class && methodCompileType.getAnnotation(anno) != null) {
                mEnv.getLog().error(
                        CompileErrors.DUPLICATED_METHOD_ANNOTATION,
                        methodCompileType
                );
            }
        });
        boolean transaction = methodCompileType.getAnnotation(Transaction.class) != null;
        Delete anno = methodCompileType.getAnnotation(Delete.class);

        String sql = anno.value();
        boolean isAutoGenerate = sql.equals(Delete.AUTO_GENERATION);

        if (!isAutoGenerate) {
            if (sql.isEmpty()) {
                mEnv.getLog().error(
                        CompileErrors.SQL_CANNOT_BE_EMPTY,
                        methodCompileType
                );
            }
        }
        List<SQLCustomParameter> parameters =
                getDeleteParameters(delegate, isAutoGenerate);
        AutoDeleteUpdateMethodTranslator translator =
                AutoDeleteUpdateMethodTranslator.create(
                        methodCompileType.getReturnType().getTypeMirror(),
                        toParameters(parameters)
                );
        if (translator == null) {
            mEnv.getLog().error(CompileErrors.DELETE_INVALID_RETURN, methodCompileType);
        }

        if (isAutoGenerate) {
            AnnotatedMethodBinder methodBinder = new DirectAutoDeleteUpdateMethodBinder(translator);
            Pair<Map<String, ParamEntity>, List<Parameter>> pair =
                    delegate.extractParameters(containing);
            return new DeleteMethod(methodCompileType,
                    null,
                    pair.first,
                    methodBinder,
                    new HandlerDeleteResultBinder(methodBinder),
                    parameters,
                    null,
                    transaction);
        }

        List<SQLCustomParameter> deleteParameters = new ArrayList<>();
        methodCompileType.getParameters().forEach(variableElement -> {
            Processor<DeleteParameter> deleteParameterProcessor =
                    new DeleteParameterProcessor(variableElement, containing, mEnv);
            deleteParameters.add(deleteParameterProcessor.process());
        });
        Processor<List<ExpressionBind>>
                processor = new SQLBindProcessor(methodCompileType, sql, mEnv);
        List<ExpressionBind> binds = processor.process();
        return new DeleteMethod(
                methodCompileType, sql,
                null, null,
                DeleteResultBinder.getInstance(),
                deleteParameters, binds,
                transaction
        );
    }

    private List<SQLCustomParameter> getDeleteParameters(AnnotateMethodProcessor processor,
                                                         boolean autoGenerate) {
        if (autoGenerate) {
            List<Parameter> parameters =
                    processor.extractRawParameters(containing);
            return toDeleteParameters(parameters);
        }
        List<SQLCustomParameter> deleteParameters = new ArrayList<>();
        methodCompileType.getParameters().forEach(variableCompileType -> {
            Processor<DeleteParameter> deleteParameterProcessor =
                    new DeleteParameterProcessor(variableCompileType, containing, mEnv);
            deleteParameters.add(deleteParameterProcessor.process());
        });
        return deleteParameters;
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
