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

import space.lingu.light.Configurations;
import space.lingu.light.DataColumn;
import space.lingu.light.SQLDataType;
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.coder.ColumnValueReader;
import space.lingu.light.compile.coder.StatementBinder;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.VariableCompileType;
import space.lingu.light.compile.struct.Configurable;
import space.lingu.light.compile.struct.Field;
import space.lingu.light.compile.struct.Nullability;
import space.lingu.light.util.StringUtils;

/**
 * @author RollW
 */
public class FieldProcessor implements Processor<Field> {
    private final VariableCompileType variableCompileType;
    private final ProcessEnv mEnv;
    private final DataColumn dataColumn;

    public FieldProcessor(VariableCompileType variableCompileType,
                          ProcessEnv env) {
        this.variableCompileType = variableCompileType;
        dataColumn = this.variableCompileType.getAnnotation(DataColumn.class);
        mEnv = env;
    }

    @Override
    public Field process() {
        final String columnName = getColumnName();
        if (StringUtils.isEmpty(columnName)) {
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

        SQLDataType preprocessType = SQLDataTypeUtils.recognizeSQLDataType(
                dataColumn.dataType(),
                variableCompileType
        );

        Configurations configurations = Configurable.createFrom(
                dataColumn.configuration(),
                variableCompileType
        );

        ColumnValueReader reader = mEnv.getBinders().findColumnReader(
                variableCompileType.getType(),
                preprocessType);
        if (reader == null) {
            mEnv.getLog().error(
                    CompileErrors.unknownInType(
                            variableCompileType,
                            preprocessType),
                    variableCompileType
            );
        }
        SQLDataType finalType = reader.getDataType();
        StatementBinder binder = mEnv.getBinders()
                .findStatementBinder(
                        variableCompileType.getType(),
                        finalType
                );
        if (binder == null) {
            mEnv.getLog().error(
                    CompileErrors.unknownOutType(variableCompileType, finalType),
                    variableCompileType
            );
        }
        if (!assignableSQLDataType(finalType, binder.getDataType())) {
            mEnv.getLog().error(
                    CompileErrors.typeMismatch(finalType, binder.getDataType()),
                    variableCompileType
            );
        }
        if (assignableSQLDataType(preprocessType, finalType)) {
            finalType = preprocessType;
        }

        // TODO: embedded type
        return new Field(
                variableCompileType,
                columnName, defaultValue,
                finalType, false,
                hasDefault, nullability,
                binder, reader,
                configurations
        );
    }

    private String getColumnName() {
        if (StringUtils.isEmpty(dataColumn.name())) {
            return variableCompileType.getName();
        }
        return dataColumn.name();
    }

    private boolean assignableSQLDataType(SQLDataType pre, SQLDataType finalType) {
        if (pre == finalType) {
            return true;
        }
        switch (pre) {
            case VARCHAR:
            case CHARS:
            case TEXT:
            case LONGTEXT:
                return finalType == SQLDataType.VARCHAR;
            case TINYINT:
            case SMALLINT:
                return finalType == SQLDataType.INT ||
                        finalType == SQLDataType.TINYINT ||
                        finalType == SQLDataType.SMALLINT;
            case REAL:
            case FLOAT:
                return finalType == SQLDataType.FLOAT ||
                        finalType == SQLDataType.REAL;
            default:
                return false;
        }
    }
}
