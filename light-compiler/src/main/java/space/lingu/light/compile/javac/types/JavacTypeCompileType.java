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

import com.squareup.javapoet.TypeName;
import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.TypeCompileType;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author RollW
 */
public class JavacTypeCompileType implements TypeCompileType {
    private final TypeMirror typeMirror;
    private final TypeElement element;

    public JavacTypeCompileType(TypeMirror typeMirror,
                                TypeElement element) {
        this.typeMirror = typeMirror;
        this.element = element;
    }

    @Override
    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    @Override
    public TypeElement getElement() {
        return element;
    }

    @Override
    public String getName() {
        if (element == null) {
            return typeMirror.toString();
        }
        return getSimpleName().toString();
    }

    @Override
    public Name getSimpleName() {
        return getElement().getSimpleName();
    }

    @Override
    public String getSignature() {
        if (element == null) {
            return typeMirror.toString();
        }
        return getQualifiedName().toString();
    }

    @Override
    public Name getQualifiedName() {
        return element.getQualifiedName();
    }

    @Override
    public TypeName toTypeName() {
        return TypeName.get(typeMirror);
    }

    @Override
    public List<MethodCompileType> getDeclaredMethods() {
        // TODO:
        return null;
    }

    @Override
    public List<MethodCompileType> getMethods() {
        return null;
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
        return "JavacTypeCompileType{" +
                "typeMirror=" + typeMirror +
                ", element=" + element +
                '}';
    }
}
