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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import space.lingu.light.DataColumn;
import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.Field;
import space.lingu.light.SQLDataType;
import space.lingu.light.util.StringUtil;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author RollW
 */
public class FieldProcessor implements Processor<Field> {
    private final VariableElement mElement;
    private final Field field;
    private final ProcessEnv mEnv;
    private final DataColumn dataColumn;

    public FieldProcessor(VariableElement element, ProcessEnv env) {
        mElement = element;
        dataColumn = mElement.getAnnotation(DataColumn.class);
        field = new Field(mElement, mElement.getSimpleName().toString());
        mEnv = env;
    }

    @Override
    public Field process() {
        if (StringUtil.isEmpty(dataColumn.name())) {
            field.setColumnName(field.getName());
        } else {
            field.setColumnName(dataColumn.name());
        }

        field.setDataType(dataColumn.dataType());
        if (field.getColumnName() == null || field.getColumnName().isEmpty()) {
            throw new LightCompileException("Field cannot have an empty column name!");
        }
        String defaultValue = dataColumn.defaultValue().equals(DataColumn.NO_DEFAULT_VALUE)
                ? null
                : dataColumn.defaultValue();
        TypeElement typeElement = (TypeElement) mElement.getEnclosingElement();
        field.setType(typeElement)
                .setDataType(recognizeSQLDataType(mElement))
                .setTypeMirror(mElement.asType())
                .setDefaultValue(defaultValue)
                .setColumnValueReader(mEnv.getBinderCache()
                        .findColumnTypeBinder(field.getTypeMirror(), field.getDataType()))
                .setStatementBinder(mEnv.getBinderCache()
                        .findColumnTypeBinder(field.getTypeMirror(), field.getDataType()));

        return field;
        // TODO 嵌套类 等处理。
    }

    private SQLDataType recognizeSQLDataType(VariableElement variable) {
        TypeMirror type = variable.asType();
        TypeName typeName = ClassName.get(type);
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

        return SQLDataType.TEXT;
    }

    private static boolean isEqualBothBox(TypeName value, TypeName type) {
        return value.equals(type) || value.equals(type.box());
    }

}
