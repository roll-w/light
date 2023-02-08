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

package space.lingu.light.compile.javac.types;

import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.javac.VariableCompileType;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;

/**
 * @author RollW
 */
public class JavacVariableCompileType implements VariableCompileType {
    private final TypeMirror typeMirror;
    private final VariableElement element;
    private final TypeCompileType type;

    public JavacVariableCompileType(TypeMirror typeMirror,
                                    VariableElement element) {
        this.typeMirror = typeMirror;
        this.element = element;

        TypeMirror varMirror = element.asType();
        TypeElement varTypeElement = ElementUtil.asTypeElement(varMirror);
        this.type = new JavacTypeCompileType(varMirror, varTypeElement);
    }

    @Override
    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    @Override
    public VariableElement getElement() {
        return element;
    }

    @Override
    public String getSignature() {
        if (element == null) {
            return typeMirror.toString();
        }
        return getSimpleName().toString();
    }

    @Override
    public String getName() {
        return getSimpleName().toString();
    }

    @Override
    public Name getSimpleName() {
        return element.getSimpleName();
    }

    @Override
    public TypeCompileType getType() {
        return type;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return element.getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return element.getAnnotationsByType(annotationType);
    }

    @Override
    public String toString() {
        return "JavacVariableCompileType{" +
                "typeMirror=" + typeMirror +
                ", element=" + element +
                ", type=" + type +
                '}';
    }
}
