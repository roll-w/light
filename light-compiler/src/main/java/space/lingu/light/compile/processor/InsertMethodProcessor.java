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

import space.lingu.light.Insert;
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.coder.annotated.binder.DirectInsertMethodBinder;
import space.lingu.light.compile.coder.annotated.binder.InsertMethodBinder;
import space.lingu.light.compile.coder.annotated.translator.InsertMethodTranslator;
import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.struct.InsertMethod;
import space.lingu.light.compile.struct.ParamEntity;
import space.lingu.light.compile.struct.Parameter;
import space.lingu.light.util.Pair;

import java.util.List;
import java.util.Map;

/**
 * Insert method processor.
 *
 * @author RollW
 */
public class InsertMethodProcessor implements Processor<InsertMethod> {
    private final MethodCompileType methodCompileType;
    private final TypeCompileType containing;
    private final ProcessEnv env;

    public InsertMethodProcessor(MethodCompileType methodCompileType,
                                 TypeCompileType containing,
                                 ProcessEnv env) {
        this.methodCompileType = methodCompileType;
        this.containing = containing;
        this.env = env;
    }

    @Override
    public InsertMethod process() {
        AnnotateMethodProcessor delegate = new AnnotateMethodProcessor(methodCompileType, env);

        Insert insertAnno = methodCompileType.getAnnotation(Insert.class);
        if (insertAnno == null) {
            // but this will never happen.
            throw new IllegalStateException("An insertion method must be annotated with @Insert.");
        }
        DaoProcessor.HANDLE_ANNOTATIONS.forEach(anno -> {
            if (anno != Insert.class && methodCompileType.getAnnotation(anno) != null) {
                env.getLog().error(
                        CompileErrors.DUPLICATED_METHOD_ANNOTATION,
                        methodCompileType
                );
            }
        });
        TypeCompileType returnType = methodCompileType.getReturnType();
        checkUnbound(returnType);

        Pair<Map<String, ParamEntity>, List<Parameter>> pair =
                delegate.extractParameters(containing);
        InsertMethodBinder binder = new DirectInsertMethodBinder(
                InsertMethodTranslator.create(
                        methodCompileType.getElement(),
                        env,
                        pair.second)
        );

        return new InsertMethod(methodCompileType,
                pair.first,
                pair.second, binder,
                insertAnno.onConflict());
    }

    private void checkUnbound(TypeCompileType typeMirror) {
        AnnotateMethodProcessor.checkUnbound(typeMirror, env);
    }
}
