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

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * @author RollW
 */
public interface TypeCompileType extends CompileType {
    @Override
    TypeMirror getTypeMirror();

    @Override
    TypeElement getElement();

    @Override
    String getName();

    @Override
    String getSignature();

    @Override
    Name getSimpleName();

    Name getQualifiedName();

    TypeName toTypeName();

    /**
     * only gets class declared
     */
    List<MethodCompileType> getDeclaredMethods();

    /**
     * get all methods except methods in Object
     */
    List<MethodCompileType> getMethods();

    TypeCompileType getSuperclass();

    List<TypeCompileType> getInterfaces();

    // TODO: add getDeclaredFields,
    //  getFields, getDeclaredConstructors, getConstructors...
}
