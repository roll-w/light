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

import space.lingu.light.Update;
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.coder.annotated.binder.AutoDeleteUpdateMethodBinder;
import space.lingu.light.compile.coder.annotated.binder.DirectAutoDeleteUpdateMethodBinder;
import space.lingu.light.compile.coder.annotated.translator.AutoDeleteUpdateMethodTranslator;
import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.struct.ParamEntity;
import space.lingu.light.compile.struct.Parameter;
import space.lingu.light.compile.struct.UpdateMethod;
import space.lingu.light.util.Pair;

import java.util.List;
import java.util.Map;

/**
 * @author RollW
 */
public class UpdateMethodProcessor implements Processor<UpdateMethod> {
    private final MethodCompileType methodCompileType;
    private final TypeCompileType containing;
    private final ProcessEnv mEnv;

    public UpdateMethodProcessor(MethodCompileType methodCompileType,
                                 TypeCompileType containing,
                                 ProcessEnv env) {
        this.methodCompileType = methodCompileType;
        this.containing = containing;
        mEnv = env;
    }

    @Override
    public UpdateMethod process() {
        AnnotateMethodProcessor delegate = new AnnotateMethodProcessor(methodCompileType, mEnv);

        Update updateAnno = methodCompileType.getAnnotation(Update.class);
        if (updateAnno == null) {
            throw new IllegalStateException("A update method must be annotated with @Update.");
        }
        DaoProcessor.sHandleAnnotations.forEach(anno -> {
            if (anno != Update.class && methodCompileType.getAnnotation(anno) != null) {
                mEnv.getLog().error(
                        CompileErrors.DUPLICATED_METHOD_ANNOTATION,
                        methodCompileType
                );
            }
        });
        Pair<Map<String, ParamEntity>, List<Parameter>> pair =
                delegate.extractParameters(containing);

        AutoDeleteUpdateMethodTranslator translator =
                AutoDeleteUpdateMethodTranslator.create(
                        methodCompileType.getReturnType().getTypeMirror(),
                        pair.second
                );
        if (translator == null) {
            mEnv.getLog().error(CompileErrors.UPDATE_INVALID_RETURN, methodCompileType);
        }
        AutoDeleteUpdateMethodBinder binder =
                new DirectAutoDeleteUpdateMethodBinder(translator);

        return new UpdateMethod(methodCompileType, pair.first,
                pair.second, binder,
                updateAnno.onConflict());
    }
}
