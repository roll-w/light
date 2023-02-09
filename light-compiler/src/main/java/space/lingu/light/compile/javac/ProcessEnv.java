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

package space.lingu.light.compile.javac;

import space.lingu.light.compile.coder.TypeBinders;
import space.lingu.light.compile.javac.types.JavacTypeCompileType;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * 处理环境
 *
 * @author RollW
 */
public class ProcessEnv {
    private final Filer filer;
    private final Elements elementUtils;
    private final Types typeUtils;
    private final Log log;
    private final TypeBinders binderCache;

    public ProcessEnv(Filer filer, Messager messager, Elements elementUtils, Types typeUtils) {
        this.filer = filer;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        log = new Log(messager);
        binderCache = new TypeBinders(this);
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    public Filer getFiler() {
        return filer;
    }

    public Types getTypeUtils() {
        return typeUtils;
    }

    public Log getLog() {
        return log;
    }

    public TypeBinders getBinders() {
        return binderCache;
    }

    public TypeCompileType getTypeCompileType(Class<?> clazz) {
        TypeElement typeElement =
                elementUtils.getTypeElement(clazz.getCanonicalName());
        TypeMirror typeMirror = typeElement.asType();
        return new JavacTypeCompileType(typeMirror, typeElement, this);
    }

    public TypeCompileType getTypeCompileType(TypeKind typeKind) {
        TypeMirror typeMirror = typeUtils.getPrimitiveType(typeKind);
        Element element = typeUtils.asElement(typeMirror);
        if (element instanceof TypeElement) {
            return new JavacTypeCompileType(typeMirror, (TypeElement) element, this);
        }
        return new JavacTypeCompileType(typeMirror, null, this);
    }

    public TypeCompileType getTypeCompileType(TypeMirror typeMirror) {
        Element element = typeUtils.asElement(typeMirror);
        if (element instanceof TypeElement) {
            return new JavacTypeCompileType(typeMirror,
                    (TypeElement) element, this);
        }
        return new JavacTypeCompileType(typeMirror,
                null, this);
    }
}
