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

package space.lingu.light.compile.struct;

import space.lingu.light.compile.coder.annotated.binder.TransactionMethodBinder;
import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.TypeCompileType;

import java.util.List;

/**
 * @author RollW
 */
public class TransactionMethod {
    private final MethodCompileType methodCompileType;
    private final List<String> paramNames;
    private final TransactionMethodBinder binder;
    private final CallType callType;

    public TransactionMethod(MethodCompileType methodCompileType,
                             List<String> paramNames,
                             TransactionMethodBinder binder,
                             CallType callType) {
        this.methodCompileType = methodCompileType;
        this.paramNames = paramNames;
        this.binder = binder;
        this.callType = callType;
    }

    public MethodCompileType getMethodCompileType() {
        return methodCompileType;
    }

    public List<String> getParamNames() {
        return paramNames;
    }

    public TransactionMethodBinder getBinder() {
        return binder;
    }

    public CallType getCallType() {
        return callType;
    }

    public TypeCompileType getReturnType() {
        return methodCompileType.getReturnType();
    }

    public enum CallType {
        DIRECT,
        DEFAULT,
        INHERITED_DEFAULT
    }
}
