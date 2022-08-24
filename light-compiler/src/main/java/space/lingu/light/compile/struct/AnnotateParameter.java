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

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * 直接使用注解无方法体的方法参数
 * @author RollW
 */
public class AnnotateParameter implements Parameter {
    private VariableElement element;
    private String name;
    private TypeElement type;
    private TypeElement wrappedType;
    private TypeMirror typeMirror;
    private boolean isMultiple;

    public AnnotateParameter() {
    }

    @Override
    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public AnnotateParameter setTypeMirror(TypeMirror typeMirror) {
        this.typeMirror = typeMirror;
        return this;
    }

    public VariableElement getElement() {
        return element;
    }

    public AnnotateParameter setElement(VariableElement element) {
        this.element = element;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    public AnnotateParameter setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public TypeElement getType() {
        return type;
    }

    public AnnotateParameter setType(TypeElement type) {
        this.type = type;
        return this;
    }

    public TypeElement getWrappedType() {
        return wrappedType;
    }

    public AnnotateParameter setWrappedType(TypeElement wrappedType) {
        this.wrappedType = wrappedType;
        return this;
    }

    public boolean isMultiple() {
        return isMultiple;
    }

    public AnnotateParameter setMultiple(boolean multiple) {
        isMultiple = multiple;
        return this;
    }
}
