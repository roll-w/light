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

import space.lingu.light.compile.javac.CompileType;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;

/**
 * @author RollW
 */
public class JavacCompileType implements CompileType {
    private final TypeMirror typeMirror;
    private final Element element;

    public JavacCompileType(TypeMirror typeMirror,
                            Element element) {
        this.typeMirror = typeMirror;
        this.element = element;
    }

    @Override
    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public Name getSimpleName() {
        return element.getSimpleName();
    }

    @Override
    public String getSignature() {
        if (element == null) {
            return typeMirror.toString();
        }
        return element.toString();
    }

    @Override
    public String getName() {
        return getSimpleName().toString();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return element.getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return element.getAnnotationsByType(annotationType);
    }
}
