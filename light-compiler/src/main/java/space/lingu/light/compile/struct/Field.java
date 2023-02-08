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
import space.lingu.light.Configurations;
import space.lingu.light.SQLDataType;
import space.lingu.light.compile.coder.ColumnValueReader;
import space.lingu.light.compile.coder.StatementBinder;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.javac.VariableCompileType;
import space.lingu.light.util.StringUtil;

import javax.lang.model.type.TypeKind;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Field - DataColumn
 * @author RollW
 */
public class Field implements Configurable {
    // TODO: makes all properties final.
    private final VariableCompileType variableCompileType;
    private final String name;
    private final String columnName;
    private final String defaultValue;
    private final SQLDataType dataType;

    private final boolean indexed;
    private final boolean hasDefault;
    private final Nullability nullability;

    private FieldGetter getter;
    private FieldSetter setter;

    private final StatementBinder statementBinder;
    private final ColumnValueReader columnValueReader;

    private final Configurations configurations;

    public Field(VariableCompileType variableCompileType,
                 String columnName,
                 String defaultValue, SQLDataType dataType,
                 boolean indexed, boolean hasDefault,
                 Nullability nullability,
                 StatementBinder statementBinder,
                 ColumnValueReader columnValueReader,
                 Configurations configurations) {
        this.variableCompileType = variableCompileType;
        this.name = variableCompileType.getName();
        this.columnName = columnName;
        this.defaultValue = defaultValue;
        this.dataType = dataType;
        this.indexed = indexed;
        this.hasDefault = hasDefault;
        this.nullability = nullability;
        this.statementBinder = statementBinder;
        this.columnValueReader = columnValueReader;
        this.configurations = configurations;
    }


    public Set<String> getPossibleCandidateName() {
        Set<String> result = new HashSet<>(Collections.singletonList(name));
        if (name.length() <= 1) {
            return result;
        }
        if (name.startsWith("_")) {
            result.add(name.substring(1));
        }
        if (name.startsWith("m") || Character.isUpperCase(name.charAt(1))) {
            result.add(StringUtil.firstLowerCase(name.substring(1)));
        }

        if (isBooleanType()) {
            String booleanGetter = tryBooleanGetter(name);
            if (booleanGetter != null) {
                result.add(booleanGetter);
            }
        }
        return result;
    }

    private static String tryBooleanGetter(String name) {
        if (name.length() > 2 && name.startsWith("is") && Character.isUpperCase(name.charAt(2))) {
            return StringUtil.firstLowerCase(name.substring(2));
        }
        if (name.length() > 3 && name.startsWith("has") && Character.isUpperCase(name.charAt(3))) {
            return StringUtil.firstLowerCase(name.substring(3));
        }
        return null;
    }

    public Set<String> setterNameCandidate() {
        final Set<String> setterNames = new HashSet<>();
        getPossibleCandidateName().forEach(s ->
                setterNames.add("set" + StringUtil.firstUpperCase(s)));
        return setterNames;
    }

    public Set<String> getterNameCandidate() {
        final Set<String> getterNames = new HashSet<>();
        getPossibleCandidateName().forEach(s -> {
            getterNames.add(s);
            getterNames.add("get" + StringUtil.firstUpperCase(s));
            if (isBooleanType()) {
                getterNames.addAll(Arrays.asList(
                        "is" + StringUtil.firstUpperCase(s),
                        "has" + StringUtil.firstUpperCase(s))
                );
            }
        });

        return getterNames;
    }

    private boolean isBooleanType() {
        TypeCompileType typeCompileType = variableCompileType.getType();
        TypeName typeName = variableCompileType.getType().toTypeName();

        return typeCompileType.getTypeMirror().getKind() == TypeKind.BOOLEAN ||
                typeName.equals(TypeName.BOOLEAN.box());
    }

    public VariableCompileType getVariableCompileType() {
        return variableCompileType;
    }

    public String getName() {
        return name;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public SQLDataType getDataType() {
        return dataType;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public boolean isHasDefault() {
        return hasDefault;
    }

    public Nullability getNullability() {
        return nullability;
    }

    public FieldGetter getGetter() {
        return getter;
    }

    public FieldSetter getSetter() {
        return setter;
    }

    public StatementBinder getStatementBinder() {
        return statementBinder;
    }

    public ColumnValueReader getColumnValueReader() {
        return columnValueReader;
    }

    public void setGetter(FieldGetter getter) {
        this.getter = getter;
    }

    public void setSetter(FieldSetter setter) {
        this.setter = setter;
    }

    @Override
    public Configurations getConfigurations() {
        return configurations;
    }

    public static class Fields {
        private final List<Field> fields;

        public Fields() {
            this.fields = Collections.emptyList();
        }

        public Fields(Field field) {
            this.fields = Collections.singletonList(field);
        }

        public Fields(List<Field> fields) {
            this.fields = Collections.unmodifiableList(fields);
        }

        public List<Field> getFields() {
            return fields;
        }

        public boolean hasField(Field field) {
            for (Field f : fields) {
                if (Objects.equals(f.getColumnName(), field.getColumnName())) {
                    return true;
                }
            }
            return false;
        }

        public Field findFieldByColumnName(String columnName) {
            List<Field> filtered = fields.stream()
                    .filter(field ->
                            field.getColumnName().equals(columnName))
                    .collect(Collectors.toList());
            if (filtered.isEmpty()) {
                return null;
            }
            return filtered.get(0);
        }

        public boolean isEmpty() {
            return fields.isEmpty();
        }
    }

    public enum CallType {
        FIELD,
        METHOD,
        CONSTRUCTOR
    }
}
