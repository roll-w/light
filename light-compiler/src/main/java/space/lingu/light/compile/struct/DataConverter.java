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

import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.TypeCompileType;

/**
 * @author RollW
 */
public class DataConverter {
    private final TypeCompileType enclosingClass;
    private final MethodCompileType element;
    private final TypeCompileType fromType;
    private final TypeCompileType toType;

    public DataConverter(TypeCompileType enclosingClass, MethodCompileType element,
                         TypeCompileType fromType, TypeCompileType toType) {
        this.enclosingClass = enclosingClass;
        this.element = element;
        this.fromType = fromType;
        this.toType = toType;
    }

    public TypeCompileType getEnclosingClass() {
        return enclosingClass;
    }

    public MethodCompileType getElement() {
        return element;
    }

    public TypeCompileType getFromType() {
        return fromType;
    }

    public TypeCompileType getToType() {
        return toType;
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
