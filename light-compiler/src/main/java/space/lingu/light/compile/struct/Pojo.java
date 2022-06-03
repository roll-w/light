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

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 代表一个用于处理的实体
 * @author RollW
 */
public class Pojo {
    private TypeElement element;
    private TypeName typeName;
    private List<Field> fields;
    private Constructor constructor;

    public Pojo() {
    }

    public TypeElement getElement() {
        return element;
    }

    public Pojo setElement(TypeElement element) {
        this.element = element;
        return this;
    }

    public TypeName getTypeName() {
        return typeName;
    }

    public Pojo setTypeName(TypeName typeName) {
        this.typeName = typeName;
        return this;
    }

    public List<Field> getFields() {
        return fields;
    }

    public Pojo setFields(List<Field> fields) {
        this.fields = fields;
        return this;
    }

    public Constructor getConstructor() {
        return constructor;
    }

    public Pojo setConstructor(Constructor constructor) {
        this.constructor = constructor;
        return this;
    }

    public Field findFieldByColumnName(String columnName) {
        if (fields == null) {
            return null;
        }
        List<Field> filtered = fields.stream()
                .filter(field ->
                        field.getColumnName().equals(columnName))
                .collect(Collectors.toList());
        if (filtered.isEmpty()) {
            return null;
        }
        return filtered.get(0);
    }
}
