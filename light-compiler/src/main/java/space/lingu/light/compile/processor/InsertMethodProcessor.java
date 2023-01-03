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
import space.lingu.light.compile.coder.annotated.translator.InsertMethodTranslator;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.InsertMethod;
import space.lingu.light.compile.struct.ParamEntity;
import space.lingu.light.compile.struct.Parameter;
import space.lingu.light.util.Pair;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;

/**
 * Insert method processor.
 *
 * @author RollW
 */
public class InsertMethodProcessor implements Processor<InsertMethod> {
    private final TypeElement mContaining;
    private final ExecutableElement mExecutable;
    private final ProcessEnv mEnv;
    private final InsertMethod insertMethod = new InsertMethod();

    public InsertMethodProcessor(ExecutableElement executable,
                                 TypeElement containing,
                                 ProcessEnv env) {
        mContaining = containing;
        mExecutable = executable;
        mEnv = env;
    }

    @Override
    public InsertMethod process() {
        AnnotateMethodProcessor delegate = new AnnotateMethodProcessor(mExecutable, mEnv);

        Insert insertAnno = mExecutable.getAnnotation(Insert.class);
        if (insertAnno == null) {
            // but this will never happen.
            throw new IllegalStateException("An insertion method must be annotated with @Insert.");
        }
        DaoProcessor.sHandleAnnotations.forEach(anno -> {
            if (anno != Insert.class && mExecutable.getAnnotation(anno) != null) {
                mEnv.getLog().error(
                        CompileErrors.DUPLICATED_METHOD_ANNOTATION,
                        mExecutable
                );
            }
        });
        TypeMirror returnType = mExecutable.getReturnType();
        checkUnbound(returnType);


        Pair<Map<String, ParamEntity>, List<Parameter>> pair =
                delegate.extractParameters(mContaining);

        return insertMethod.setElement(mExecutable)
                .setReturnType(returnType)
                .setOnConflict(insertAnno.onConflict())
                .setEntities(pair.first)
                .setParameters(pair.second)
                .setBinder(
                        new DirectInsertMethodBinder(
                                InsertMethodTranslator.create(
                                        insertMethod.getElement(),
                                        mEnv,
                                        insertMethod.getParameters()))
                );
    }

    private void checkUnbound(TypeMirror typeMirror) {
        AnnotateMethodProcessor.checkUnbound(typeMirror, mEnv, mExecutable);
    }
}
