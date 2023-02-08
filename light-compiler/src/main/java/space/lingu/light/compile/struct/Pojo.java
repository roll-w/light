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
import space.lingu.light.compile.javac.TypeCompileType;

/**
 * 代表一个用于处理的实体
 *
 * @author RollW
 */
public class Pojo {
    private final TypeCompileType typeCompileType;
    private final TypeName typeName;
    private final Field.Fields fields;
    private final Constructor constructor;

    public Pojo(TypeCompileType typeCompileType,
                Field.Fields fields, Constructor constructor) {
        this.typeCompileType = typeCompileType;
        this.typeName = TypeName.get(typeCompileType.getTypeMirror());
        this.fields = fields;
        this.constructor = constructor;
    }

    public TypeCompileType getTypeCompileType() {
        return typeCompileType;
    }

    public TypeName getTypeName() {
        return typeName;
    }

    public Field.Fields getFields() {
        return fields;
    }

    public Constructor getConstructor() {
        return constructor;
    }

    public Field findFieldByColumnName(String columnName) {
        return fields.findFieldByColumnName(columnName);
    }
}
