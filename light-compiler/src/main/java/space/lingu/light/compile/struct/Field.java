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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import space.lingu.light.Configurations;
import space.lingu.light.SQLDataType;
import space.lingu.light.compile.coder.ColumnValueReader;
import space.lingu.light.compile.coder.StatementBinder;
import space.lingu.light.util.StringUtil;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;

/**
 * Field - DataColumn
 * @author RollW
 */
public class Field implements Configurable {
    // TODO: makes all properties final.
    private final VariableElement element;
    private final String name;
    private TypeElement type;
    private TypeMirror typeMirror;

    private String columnName;
    private String defaultValue = null;

    private SQLDataType dataType;

    private boolean indexed = false;
    private boolean hasDefault;
    private Nullability nullability;

    private FieldGetter getter;
    private FieldSetter setter;

    private StatementBinder statementBinder;
    private ColumnValueReader columnValueReader;

    private Configurations configurations;

    public Field(VariableElement element, String name) {
        this.element = element;
        this.name = name;
        columnName = name;
    }

    public List<String> getPossibleCandidateName() {
        List<String> result = new ArrayList<>(Collections.singletonList(name));
        if (name.length() > 1) {
            if (name.startsWith("_")) {
                result.add(name.substring(1));
            }
            if (name.startsWith("m") || Character.isUpperCase(name.charAt(1))) {
                result.add(StringUtil.firstLowerCase(name.substring(1)));
            }
            TypeName typeName = ClassName.get(type);
            if (typeMirror.getKind() == TypeKind.BOOLEAN || typeName.equals(TypeName.BOOLEAN.box())) {
                if (name.length() > 2 && name.startsWith("is") && Character.isUpperCase(name.charAt(2))) {
                    result.add(StringUtil.firstLowerCase(name.substring(2)));
                }
                if (name.length() > 3 && name.startsWith("has") && Character.isUpperCase(name.charAt(3))) {
                    result.add(StringUtil.firstLowerCase(name.substring(3)));
                }
            }
        }
        return result;
    }

    public List<String> setterNameCandidate() {
        final List<String> setterNames = new ArrayList<>();
        getPossibleCandidateName().forEach(s ->
                setterNames.add("set" + StringUtil.firstUpperCase(s)));
        return setterNames;
    }

    public List<String> getterNameCandidate() {
        final List<String> getterNames = new ArrayList<>();
        getPossibleCandidateName().forEach(s -> {
            getterNames.add(s);
            getterNames.add("get" + StringUtil.firstUpperCase(s));
            TypeName typeName = TypeName.get(typeMirror);
            if (typeMirror.getKind() == TypeKind.BOOLEAN ||
                    typeName.equals(TypeName.BOOLEAN.box())) {
                getterNames.addAll(Arrays.asList(
                        "is" + StringUtil.firstUpperCase(s),
                        "has" + StringUtil.firstUpperCase(s))
                );
            }
        });

        return getterNames;
    }

    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public Field setTypeMirror(TypeMirror typeMirror) {
        this.typeMirror = typeMirror;
        return this;
    }

    public StatementBinder getStatementBinder() {
        return statementBinder;
    }

    public Field setStatementBinder(StatementBinder statementBinder) {
        this.statementBinder = statementBinder;
        return this;
    }

    public ColumnValueReader getColumnValueReader() {
        return columnValueReader;
    }

    public Field setColumnValueReader(ColumnValueReader columnValueReader) {
        this.columnValueReader = columnValueReader;
        return this;
    }

    public SQLDataType getDataType() {
        return dataType;
    }

    public Field setDataType(SQLDataType dataType) {
        this.dataType = dataType;
        return this;
    }

    public VariableElement getElement() {
        return element;
    }

    public String getName() {
        return name;
    }

    public TypeElement getType() {
        return type;
    }

    public Field setType(TypeElement type) {
        this.type = type;
        return this;
    }

    public String getColumnName() {
        return columnName;
    }

    public Field setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Field setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public Field setIndexed(boolean indexed) {
        this.indexed = indexed;
        return this;
    }

    public boolean isHasDefault() {
        return hasDefault;
    }

    public Field setHasDefault(boolean hasDefault) {
        this.hasDefault = hasDefault;
        return this;
    }

    public FieldGetter getGetter() {
        return getter;
    }

    public Field setGetter(FieldGetter getter) {
        this.getter = getter;
        return this;
    }

    public FieldSetter getSetter() {
        return setter;
    }

    public Field setSetter(FieldSetter setter) {
        this.setter = setter;
        return this;
    }

    public Nullability getNullability() {
        return nullability;
    }

    public Field setNullability(Nullability nullability) {
        this.nullability = nullability;
        return this;
    }

    @Override
    public Configurations getConfigurations() {
        if (configurations == null) {
            return Configurations.empty();
        }
        return configurations;
    }

    public Field setConfigurations(Configurations configurations) {
        this.configurations = configurations;
        return this;
    }

    public static class Fields {
        public final List<Field> fields;

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
    }

    public enum CallType {
        FIELD,
        METHOD,
        CONSTRUCTOR
    }
}
