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
import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.coder.annotated.binder.DirectInsertMethodBinder;
import space.lingu.light.compile.coder.annotated.translator.InsertMethodTranslator;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.AnnotateParameter;
import space.lingu.light.compile.struct.InsertMethod;
import space.lingu.light.compile.struct.ParamEntity;
import space.lingu.light.util.Pair;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Map;

/**
 * 插入方法处理器
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
        // TODO
        AnnotateMethodProcessor delegate = new AnnotateMethodProcessor(mExecutable, mEnv);

        Insert insertAnno = mExecutable.getAnnotation(Insert.class);
        if (insertAnno == null) {
            throw new LightCompileException("An insertion method must be annotated with @Insert.");
        }
        DaoProcessor.PROCESS_ANNOTATIONS.forEach(anno -> {
            if (anno != Insert.class) {
                if (mExecutable.getAnnotation(anno) != null) {
                    throw new LightCompileException("Only can have one of annotations below : @Insert, @Update, @Query, @Delete.");
                }
            }
        });

        Pair<Map<String, ParamEntity>, List<AnnotateParameter>> pair =
                delegate.extractParameters(mContaining);

        return insertMethod.setElement(mExecutable)
                .setReturnType(mExecutable.getReturnType())
                .setOnConflict(insertAnno.onConflict())
                .setEntities(pair.first)
                .setParameters(pair.second)
                .setBinder(new DirectInsertMethodBinder(
                        InsertMethodTranslator.create(
                                insertMethod.getReturnType(),
                                mEnv,
                                insertMethod.getParameters())));
    }
}
