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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author RollW
 */
public class DataConverter {
    private TypeElement enclosingClass;
    private ExecutableElement element;
    private TypeMirror fromType;
    private TypeMirror toType;

    public TypeElement getEnclosingClass() {
        return enclosingClass;
    }

    public DataConverter setEnclosingClass(TypeElement enclosingClass) {
        this.enclosingClass = enclosingClass;
        return this;
    }

    public ExecutableElement getElement() {
        return element;
    }

    public DataConverter setElement(ExecutableElement element) {
        this.element = element;
        return this;
    }

    public TypeMirror getFromType() {
        return fromType;
    }

    public DataConverter setFromType(TypeMirror fromType) {
        this.fromType = fromType;
        return this;
    }

    public TypeMirror getToType() {
        return toType;
    }

    public DataConverter setToType(TypeMirror toType) {
        this.toType = toType;
        return this;
    }

    public String getMethodName() {
        return element.getSimpleName().toString();
    }

    @Override
    public String toString() {
        return "DataConverter: " +
                "Defined in [" + enclosingClass + "#" + getMethodName() +
                "], from " + fromType + " to " + toType;
    }
}
