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
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import space.lingu.light.Configurations;
import space.lingu.light.DataColumn;
import space.lingu.light.SQLDataType;
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.coder.ColumnValueReader;
import space.lingu.light.compile.coder.StatementBinder;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.Configurable;
import space.lingu.light.compile.struct.Field;
import space.lingu.light.compile.struct.Nullability;
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
            // should not happen
            throw new IllegalArgumentException("Field cannot have an empty column name.");
        }
        boolean hasDefault = !dataColumn.defaultValue().equals(DataColumn.NO_DEFAULT_VALUE);

        String defaultValue = dataColumn.defaultValue().equals(DataColumn.DEFAULT_VALUE_NULL)
                ? null
                : dataColumn.defaultValue();

        Nullability nullability = dataColumn.nullable()
                ? Nullability.NULLABLE
                : Nullability.NONNULL;

        TypeElement typeElement = (TypeElement) mElement.getEnclosingElement();
        Configurations configurations = Configurable.createFrom(dataColumn.configuration());

        field.setType(typeElement)
                .setDataType(recognizeSQLDataType(mElement))
                .setTypeMirror(mElement.asType())
                .setNullability(nullability)
                .setHasDefault(hasDefault)
                .setConfigurations(configurations)
                .setDefaultValue(defaultValue)
                .setIndexed(dataColumn.index());
        StatementBinder binder = mEnv.getBinders()
                .findStatementBinder(field.getTypeMirror(), field.getDataType());
        ColumnValueReader reader = mEnv.getBinders()
                .findColumnReader(field.getTypeMirror(), field.getDataType());

        // todo
        if (binder == null || reader == null) {
            mEnv.getLog().error(
                    CompileErrors.unknownType(mElement.asType()),
                    mElement
            );
        }
        return field
                .setColumnValueReader(reader)
                .setStatementBinder(binder);
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
        if (isEqualArray(typeName, TypeName.BYTE)) {
            return SQLDataType.BINARY;
        }
        return SQLDataType.VARCHAR;
    }

    private static boolean isEqualBothBox(TypeName value, TypeName type) {
        return value.equals(type) || value.equals(type.box());
    }

    @SuppressWarnings("all")
    private static boolean isEqualArray(TypeName value, TypeName type) {
        return value.equals(ArrayTypeName.of(type));
    }

}
