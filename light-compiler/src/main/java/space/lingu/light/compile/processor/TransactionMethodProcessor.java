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

import space.lingu.light.compile.coder.annotated.binder.DirectTransactionMethodBinder;
import space.lingu.light.compile.coder.annotated.translator.TransactionMethodTranslator;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.TransactionMethod;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author RollW
 */
public class TransactionMethodProcessor implements Processor<TransactionMethod> {
    private final TypeElement mContaining;
    private final ExecutableElement mExecutable;
    private final ProcessEnv mEnv;
    private final TransactionMethod method = new TransactionMethod();

    public TransactionMethodProcessor(ExecutableElement executable,
                                      TypeElement containing,
                                      ProcessEnv env) {
        mContaining = containing;
        mExecutable = executable;
        mEnv = env;
    }

    @Override
    public TransactionMethod process() {
        List<String> paramNames = new ArrayList<>();
        mExecutable.getParameters().forEach(variableElement ->
                paramNames.add(variableElement.getSimpleName().toString()));
        TransactionMethod.CallType callType = getCallType(mExecutable, mContaining);
        method.setCallType(callType);
        TransactionMethodTranslator transactionMethodTranslator =
                new TransactionMethodTranslator(
                        mExecutable.getSimpleName().toString(),
                        callType
                );

        return method.setElement(mExecutable)
                .setReturnType(mExecutable.getReturnType())
                .setParamNames(paramNames)
                .setBinder(new DirectTransactionMethodBinder(transactionMethodTranslator));
    }

    private static TransactionMethod.CallType getCallType(ExecutableElement executableElement,
                                                          TypeElement typeElement) {
        if (!ElementUtil.isDefault(executableElement)) {
            return TransactionMethod.CallType.DIRECT;
        }
        if (ElementUtil.isInterface(typeElement)) {
            return TransactionMethod.CallType.DEFAULT;
        }
        return TransactionMethod.CallType.INHERITED_DEFAULT;
    }
}
