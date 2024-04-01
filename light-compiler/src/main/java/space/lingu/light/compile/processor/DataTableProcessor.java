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
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.Warnings;
import space.lingu.light.compile.javac.ElementUtils;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.struct.Configurable;
import space.lingu.light.compile.struct.DataTable;
import space.lingu.light.compile.struct.Field;
import space.lingu.light.compile.struct.Index;
import space.lingu.light.compile.struct.Pojo;
import space.lingu.light.compile.struct.PrimaryKey;
import space.lingu.light.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author RollW
 */
public class DataTableProcessor implements Processor<DataTable> {
    private final TypeCompileType typeCompileType;
    private final space.lingu.light.DataTable anno;
    private final ProcessEnv env;

    public DataTableProcessor(TypeCompileType typeCompileType,
                              ProcessEnv env) {
        this.env = env;
        this.typeCompileType = typeCompileType;
        this.anno = typeCompileType.getAnnotation(space.lingu.light.DataTable.class);
        if (anno == null) {
            env.getLog().error(CompileErrors.DATA_TABLE_NOT_ANNOTATED, typeCompileType);
        }
    }

    @Override
    public DataTable process() {
        PojoProcessor processor = new PojoProcessor(typeCompileType, env);
        final String tableName = getTableName(anno);

        Pojo pojo = processor.process();
        Field.Fields fields = pojo.getFields();

        checkColumnName(fields);

        env.getLog().error(
                pojo.getFields().isEmpty(),
                CompileErrors.TABLE_NO_FIELDS,
                typeCompileType
        );

        PrimaryKey primaryKey = findPrimaryKey(pojo.getFields());
        env.getLog().warn(
                primaryKey == PrimaryKey.MISSING,
                Warnings.PRIMARY_KEY_NOT_FOUND,
                typeCompileType,
                typeCompileType.getName()
        );

        Configurations configurations = Configurable.createFrom(
                anno.configuration(),
                typeCompileType
        );
        List<Index> indices = processIndices(tableName, pojo.getFields());
        return new DataTable(
                typeCompileType,
                fields, pojo.getConstructor(),
                tableName, primaryKey,
                indices, Collections.emptyList(),
                configurations
        );
    }

    @SuppressWarnings({"deprecation"})
    private String getTableName(space.lingu.light.DataTable annotation) {
        String name = annotation.name();
        if (!name.isEmpty()) {
            return name;
        }
        // TODO: Remove this in the future
        String tableName = annotation.tableName();
        if (!tableName.isEmpty()) {
            return tableName;
        }
        return typeCompileType.getName();
    }

    private PrimaryKey findPrimaryKey(Field.Fields fields) {
        List<PrimaryKey> primaryKeys = new ArrayList<>();
        primaryKeys.add(getPrimaryKeyFromPrimaryKey(fields));
        return choosePrimaryKey(primaryKeys, typeCompileType);
    }

    private PrimaryKey getPrimaryKeyFromPrimaryKey(Field.Fields fields) {
        if (fields == null || fields.isEmpty()) {
            return PrimaryKey.MISSING;
        }
        List<Field> hasAnnotationFields = new ArrayList<>();
        fields.getFields().forEach(field -> {
            space.lingu.light.PrimaryKey keyAnno =
                    field.getVariableCompileType().getAnnotation(space.lingu.light.PrimaryKey.class);
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
                    .getVariableCompileType()
                    .getAnnotation(space.lingu.light.PrimaryKey.class)
                    .autoGenerate();
        }
        return new PrimaryKey(
                typeCompileType,
                new Field.Fields(hasAnnotationFields),
                autoGenerate
        );
    }

    private PrimaryKey choosePrimaryKey(List<PrimaryKey> candidates, TypeCompileType typeCompileType) {
        List<PrimaryKey> filtered = candidates.stream()
                .filter(primaryKey -> {
                    if (primaryKey == PrimaryKey.MISSING ||
                            primaryKey.getDeclaredIn() == null) {
                        return false;
                    }
                    return ElementUtils.equalTypeElement(
                            primaryKey.getDeclaredIn().getElement(),
                            typeCompileType.getElement()
                    );
                })
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return PrimaryKey.MISSING;
        }
        if (filtered.size() == 1) {
            return filtered.get(0);
        }

        env.getLog().error(CompileErrors.MULTIPLE_PRIMARY_KEY_FOUND, typeCompileType);
        return null;
    }

    private void checkColumnName(Field.Fields fields) {
        Set<String> names = new HashSet<>();
        fields.getFields().forEach(field -> {
            if (names.contains(field.getColumnName())) {
                env.getLog().error(
                        CompileErrors.duplicatedTableColumnName(field.getColumnName()),
                        field.getVariableCompileType()
                );
                return;
            }
            names.add(field.getColumnName());
        });
    }

    private List<Index> processIndices(String tableName,
                                       Field.Fields fields) {
        List<Index> indices = new ArrayList<>();
        for (space.lingu.light.Index index : anno.indices()) {
            Configurations configurations =
                    Configurations.createFrom(index.configurations());
            List<Field> indexFields = new ArrayList<>();
            for (String columnName : index.value()) {
                Field field = fields.findFieldByColumnName(columnName);
                if (field == null) {
                    env.getLog().error(CompileErrors.cannotFoundIndexField(columnName), typeCompileType);
                    continue;
                }
                indexFields.add(field);
            }
            List<String> fieldNames =
                    indexFields.stream()
                            .map(Field::getColumnName)
                            .collect(Collectors.toList());
            String indexName = generateIndexName(
                    index.name(),
                    tableName,
                    fieldNames);
            Index processedIndex = new Index(
                    indexName,
                    index.unique(),
                    new Field.Fields(indexFields),
                    Arrays.asList(index.orders()),
                    configurations
            );
            indices.add(processedIndex);
        }

        for (Field field : fields.getFields()) {
            if (field.isIndexed()) {
                Index index = new Index(
                        generateIndexName(
                                null,
                                tableName,
                                Collections.singletonList(field.getColumnName())),
                        false,
                        new Field.Fields(field),
                        Collections.emptyList(),
                        field.getConfigurations()
                );
                indices.add(index);
            }
        }
        // with same name, prefer defined in @Index
        Set<String> indexNames = new HashSet<>();
        return indices.stream().filter(index -> {
            if (!indexNames.contains(index.getName())) {
                indexNames.add(index.getName());
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    private String generateIndexName(String defaultName,
                                     String tableName,
                                     List<String> columnNames) {
        if (!StringUtils.isEmpty(defaultName)) {
            return defaultName;
        }
        StringJoiner columns = new StringJoiner("_");
        columnNames.forEach(columns::add);
        return Index.DEFAULT_PREFIX + "_" + tableName + "_" + columns;
    }
}
