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

package space.lingu.light.sql;

import space.lingu.light.Configurations;
import space.lingu.light.SQLDataType;
import space.lingu.light.struct.TableColumn;

/**
 * @author RollW
 */
public abstract class GeneralDialectProvider extends AsciiSQLGenerator
        implements DialectProvider, SQLGenerator {

    protected String createColumn(TableColumn column) {
        StringBuilder builder = new StringBuilder(escapeParam(column.getName()))
                .append(" ");
        final String nonNull = column.isNullable()
                ? ""
                : " " + notNullDeclare();
        builder.append(getDefaultTypeDeclaration(column.getDataType(),
                        column.getConfigurations()))
                .append(nonNull);
        if (column.isAutoGenerate()) {
            builder.append(" ").append(autoIncrementDeclare());
        }
        if (column.isHasDefaultValue()) {
            builder.append(" DEFAULT");
            if (column.isDefaultValueNull()) {
                builder.append(" NULL");
            } else {
                builder.append(" ")
                        .append(column.getDefaultValueWithProcess());
            }
        }

        return builder.toString();
    }

    protected abstract String notNullDeclare();

    protected abstract String autoIncrementDeclare();

    protected abstract String getDefaultTypeDeclaration(SQLDataType dataType,
                                                        Configurations configurations);


}
