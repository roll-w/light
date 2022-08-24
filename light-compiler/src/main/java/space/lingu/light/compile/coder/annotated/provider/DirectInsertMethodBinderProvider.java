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

package space.lingu.light.compile.coder.annotated.provider;

import space.lingu.light.compile.coder.annotated.binder.DirectInsertMethodBinder;
import space.lingu.light.compile.coder.annotated.binder.InsertMethodBinder;
import space.lingu.light.compile.coder.annotated.translator.InsertMethodTranslator;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.Parameter;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * @author RollW
 */
public class DirectInsertMethodBinderProvider implements InsertMethodBinderProvider {
    private final ProcessEnv mEnv;

    public DirectInsertMethodBinderProvider(ProcessEnv env) {
        mEnv = env;
    }

    @Override
    public boolean matches(TypeMirror typeMirror) {
        return true;
    }

    @Override
    public InsertMethodBinder provide(ExecutableElement executableElement,
                                      List<Parameter> params) {
        return new DirectInsertMethodBinder(InsertMethodTranslator.create(
                executableElement,
                mEnv,
                params)
        );
    }

}
