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

package space.lingu.light.compile.processor;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.TypeName;
import space.lingu.light.SQLDataType;
import space.lingu.light.compile.javac.CompileType;

import javax.lang.model.type.TypeMirror;

/**
 * @author RollW
 */
public final class SQLDataTypeUtils {
    public static SQLDataType recognizeSQLDataType(SQLDataType sqlDataType,
                                                   CompileType compileType) {
        if (sqlDataType != null && sqlDataType != SQLDataType.UNDEFINED) {
            return sqlDataType;
        }
        TypeMirror type = compileType.getTypeMirror();
        TypeName typeName = TypeName.get(type);
        if (isEqualBothBox(typeName, TypeName.INT)) {
            return SQLDataType.INT;
        }
        if (isEqualBothBox(typeName, TypeName.SHORT)) {
            return SQLDataType.INT;
        }
        if (isEqualBothBox(typeName, TypeName.LONG)) {
            return SQLDataType.LONG;
        }
        if (isEqualBothBox(typeName, TypeName.BYTE)) {
            return SQLDataType.INT;
        }
        if (isEqualBothBox(typeName, TypeName.CHAR)) {
            return SQLDataType.CHAR;
        }
        if (isEqualBothBox(typeName, TypeName.DOUBLE)) {
            return SQLDataType.DOUBLE;
        }
        if (isEqualBothBox(typeName, TypeName.FLOAT)) {
            return SQLDataType.FLOAT;
        }
        if (isEqualBothBox(typeName, TypeName.BOOLEAN)) {
            return SQLDataType.BOOLEAN;
        }
        if (isEqualArray(typeName, TypeName.BYTE)) {
            return SQLDataType.BINARY;
        }
        if (STRING.equals(typeName)) {
            return SQLDataType.VARCHAR;
        }
        return SQLDataType.UNDEFINED;
    }

    private static final TypeName STRING = TypeName.get(String.class);

    private static boolean isEqualBothBox(TypeName value, TypeName type) {
        return value.equals(type) || value.equals(type.box());
    }

    @SuppressWarnings("all")
    private static boolean isEqualArray(TypeName value, TypeName type) {
        return value.equals(ArrayTypeName.of(type));
    }

    private SQLDataTypeUtils() {
    }
}
