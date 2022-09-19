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
import space.lingu.light.LightIgnore;
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.Warnings;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.*;

import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author RollW
 */
public class DataTableProcessor implements Processor<DataTable> {
    private final TypeElement mElement;
    private final DataTable dataTable = new DataTable();
    private final space.lingu.light.DataTable anno;
    private final ProcessEnv mEnv;

    public DataTableProcessor(TypeElement element, ProcessEnv env) {
        mElement = element;
        anno = element.getAnnotation(space.lingu.light.DataTable.class);
        if (anno == null) {
            throw new LightCompileException("A data table class must be annotated with @DataTable.");
        }
        mEnv = env;
    }

    @Override
    public DataTable process() {
        PojoProcessor processor = new PojoProcessor(mElement, mEnv);
        final String tableName = anno.tableName().isEmpty()
                ? mElement.getSimpleName().toString()
                : anno.tableName();

        Pojo pojo = processor.process();
        dataTable.setElement(mElement)
                .setTableName(tableName)
                .setConstructor(pojo.getConstructor())
                .setTypeName(pojo.getTypeName())
                .setFields(pojo.getFields());
        checkColumnName(dataTable);
        if (pojo.getFields().isEmpty()) {
            mEnv.getLog().error(CompileErrors.TABLE_NO_FIELDS, mElement);
        }

        PrimaryKey primaryKey = findPrimaryKey(pojo.getFields());
        if (primaryKey == PrimaryKey.MISSING &&
                mElement.getAnnotation(LightIgnore.class) == null) {
            mEnv.getLog().warn(Warnings.PRIMARY_KEY_NOT_FOUND, mElement);
        }
        Configurations configurations = Configurable.createFrom(anno.configuration());
        return dataTable
                .setConfigurations(configurations)
                .setPrimaryKey(primaryKey)
                .setIndices(processIndices(pojo.getFields()));
    }

    private PrimaryKey findPrimaryKey(List<Field> fields) {
        List<PrimaryKey> primaryKeys = new ArrayList<>();

        primaryKeys.add(getPrimaryKeyFromPrimaryKey(fields));
        primaryKeys.addAll(getPrimaryKeysFromDataEntity(fields));

        return choosePrimaryKey(primaryKeys, mElement);
    }

    private List<PrimaryKey> getPrimaryKeysFromDataEntity(List<Field> fields) {
        List<String> keys = Arrays.asList(anno.primaryKeys());
        if (keys.isEmpty()) {
            return new ArrayList<>();
        }
        mEnv.getLog().warn(
                "You are still using primaryKeys() to define the primary key. This will be removed in the future." +
                        Warnings.DEPRECATED,
                mElement);

        List<PrimaryKey> primaryKeys = new ArrayList<>();
        List<Field> primaryKeyFields = new ArrayList<>();

        keys.forEach(s -> {
            Field field = findFieldByColumnName(fields, s);
            if (field == null) {
                mEnv.getLog().error(
                        CompileErrors.cannotFoundPrimaryKeyField(s),
                        mElement);
                return;
            }
            primaryKeyFields.add(field);
        });
        primaryKeys.add(
                new PrimaryKey(
                        mElement,
                        new Field.Fields(primaryKeyFields),
                        false)
        );

        return primaryKeys;
    }

    private PrimaryKey getPrimaryKeyFromPrimaryKey(List<Field> fields) {
        if (fields == null || fields.isEmpty()) {
            return PrimaryKey.MISSING;
        }
        List<Field> hasAnnotationFields = new ArrayList<>();
        fields.forEach(field -> {
            space.lingu.light.PrimaryKey keyAnno =
                    field.getElement().getAnnotation(space.lingu.light.PrimaryKey.class);
            if (keyAnno == null) {
                return;
            }
            hasAnnotationFields.add(field);
        });
        if (hasAnnotationFields.isEmpty()) {
            return PrimaryKey.MISSING;
        }
        boolean autoGenerate = false;
        if (hasAnnotationFields.size() == 1) {
            autoGenerate = hasAnnotationFields.get(0)
                    .getElement()
                    .getAnnotation(space.lingu.light.PrimaryKey.class)
                    .autoGenerate();
        }
        return new PrimaryKey(
                mElement,
                new Field.Fields(hasAnnotationFields),
                autoGenerate
        );
    }

    private PrimaryKey choosePrimaryKey(List<PrimaryKey> candidates, TypeElement typeElement) {
        List<PrimaryKey> filtered = candidates.stream()
                .filter(primaryKey ->
                        ElementUtil.equalTypeElement(primaryKey.getDeclaredIn(), typeElement))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return PrimaryKey.MISSING;
        }
        if (filtered.size() == 1) {
            return filtered.get(0);
        }

        mEnv.getLog().error("Multiple primary keys were found, there are only one can be used @PrimaryKey.", typeElement);
        throw new LightCompileException("Multiple primary keys were found, there are only one can be used @PrimaryKey.");
    }

    private void checkColumnName(DataTable dataTable) {
        List<Field> fields = dataTable.getFields();
        Set<String> names = new HashSet<>();
        fields.forEach(field -> {
            if (names.contains(field.getColumnName())) {
                mEnv.getLog().error(
                        CompileErrors.duplicatedTableColumnName(field.getColumnName()),
                        field.getElement()
                );
                return;
            }
            names.add(field.getColumnName());
        });
    }

    private List<Index> processIndices(List<Field> fields) {
        List<Index> indices = new ArrayList<>();
        for (space.lingu.light.Index index : anno.indices()) {
            Configurations configurations =
                    Configurations.createFrom(index.configurations());
            List<Field> indexFields = new ArrayList<>();
            for (String columnName : index.value()) {
                Field field = findFieldByColumnName(fields, columnName);
                if (field == null) {
                    mEnv.getLog().error(CompileErrors.cannotFoundIndexField(columnName), mElement);
                    continue;
                }
                indexFields.add(field);
            }

            Index processedIndex = new Index(index.name(),
                    index.unique(),
                    new Field.Fields(indexFields),
                    Arrays.asList(index.orders()),
                    configurations
            );
            indices.add(processedIndex);
        }

        return indices;
    }

    private static Field findFieldByColumnName(List<Field> fields, String columnName) {
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
