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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * @author RollW
 */
public class TransactionMethod {
    private ExecutableElement element;
    private TypeMirror returnType;
    private List<String> paramNames;
    private TransactionMethodBinder binder;
    private CallType callType;

    public TransactionMethod() {
    }

    public CallType getCallType() {
        return callType;
    }

    public TransactionMethod setCallType(CallType callType) {
        this.callType = callType;
        return this;
    }

    public ExecutableElement getElement() {
        return element;
    }

    public TransactionMethod setElement(ExecutableElement element) {
        this.element = element;
        return this;
    }

    public TypeMirror getReturnType() {
        return returnType;
    }

    public TransactionMethod setReturnType(TypeMirror returnType) {
        this.returnType = returnType;
        return this;
    }

    public List<String> getParamNames() {
        return paramNames;
    }

    public TransactionMethod setParamNames(List<String> paramNames) {
        this.paramNames = paramNames;
        return this;
    }

    public TransactionMethodBinder getBinder() {
        return binder;
    }

    public TransactionMethod setBinder(TransactionMethodBinder binder) {
        this.binder = binder;
        return this;
    }

    public enum CallType {
        DIRECT,
        DEFAULT,
        INHERITED_DEFAULT
    }
}
