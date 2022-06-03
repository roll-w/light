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
import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.coder.annotated.translator.DeleteUpdateMethodTranslator;
import space.lingu.light.compile.coder.annotated.binder.DirectDeleteUpdateMethodBinder;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.AnnotateParameter;
import space.lingu.light.compile.struct.DeleteMethod;
import space.lingu.light.compile.struct.ParamEntity;
import space.lingu.light.util.Pair;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Map;

/**
 * @author RollW
 */
public class DeleteMethodProcessor implements Processor<DeleteMethod> {
    private final ExecutableElement mElement;
    private final TypeElement mContaining;
    private final ProcessEnv mEnv;
    private final DeleteMethod method = new DeleteMethod();

    public DeleteMethodProcessor(ExecutableElement element,
                                 TypeElement containing,
                                 ProcessEnv env) {
        mElement = element;
        mContaining = containing;
        mEnv = env;
    }

    @Override
    public DeleteMethod process() {
        AnnotateMethodProcessor delegate = new AnnotateMethodProcessor(mElement, mEnv);

        DaoProcessor.PROCESS_ANNOTATIONS.forEach(anno -> {
            if (anno != Delete.class) {
                if (mElement.getAnnotation(anno) != null) {
                    throw new LightCompileException("Only can have one of annotations below : @Insert, @Update, @Query, @Delete.");
                }
            }
        });
        Pair<Map<String, ParamEntity>, List<AnnotateParameter>> pair =
                delegate.extractParameters(mContaining);

        return method.setElement(mElement)
                .setEntities(pair.first)
                .setParameters(pair.second)
                .setReturnType(mElement.getReturnType())
                .setBinder(new DirectDeleteUpdateMethodBinder(
                        DeleteUpdateMethodTranslator.create(
                                method.getReturnType(),
                                mEnv,
                                method.getParameters())));
    }
}
