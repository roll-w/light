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
        primaryKeys.addAll(getPrimaryKeysFromPrimaryKey(fields));
        primaryKeys.addAll(getPrimaryKeysFromDataEntity(fields));

        return choosePrimaryKey(primaryKeys, mElement);
    }

    private List<PrimaryKey> getPrimaryKeysFromDataEntity(List<Field> fields) {
        List<String> keys = Arrays.asList(anno.primaryKeys());
        if (keys.isEmpty()) {
            return new ArrayList<>();
        }

        List<PrimaryKey> primaryKeys = new ArrayList<>();
        List<Field> primaryKeyFields = new ArrayList<>();

        keys.forEach(s ->
                primaryKeyFields.add(findFieldByColumnName(fields, s)));
        primaryKeys.add(new PrimaryKey(mElement,
                new Field.Fields(primaryKeyFields),
                false));

        return primaryKeys;
    }

    private List<PrimaryKey> getPrimaryKeysFromPrimaryKey(List<Field> fields) {
        List<PrimaryKey> primaryKeys = new ArrayList<>();
        fields.forEach(field -> {
            space.lingu.light.PrimaryKey keyAnno =
                    field.getElement().getAnnotation(space.lingu.light.PrimaryKey.class);
            if (keyAnno == null) {
                return;
            }
            primaryKeys.add(
                    new PrimaryKey(mElement, new Field.Fields(field),
                            keyAnno.autoGenerate())
            );
        });
        return primaryKeys;
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

        // TODO 在无需抛出编译异常的位置使用日志输出错误信息
        mEnv.getLog().error("Multiple primary keys were found, there are only one can be used @PrimaryKey.", typeElement);
        throw new LightCompileException("Multiple primary keys were found, there are only one can be used @PrimaryKey.");
    }

    private void checkColumnName(DataTable dataTable) {
        List<Field> fields = dataTable.getFields();
        Set<String> names = new HashSet<>();
        fields.forEach(field -> {
            if (names.contains(field.getColumnName())) {
                throw new LightCompileException("Column names can not be repeatable.");
            }
            names.add(field.getColumnName());
        });
    }

    private List<Index> processIndices(List<Field> fields) {
        List<Index> indices = new ArrayList<>();
        for (space.lingu.light.Index index : anno.indices()) {
            List<Field> indexFields = new ArrayList<>();
            for (String s : index.value()) {
                Field field = findFieldByColumnName(fields, s);
                if (field == null) {
                    throw new LightCompileException("Please check indices' column name.");
                }
                indexFields.add(field);
            }

            indices.add(new Index(index.name(), index.unique(),
                    new Field.Fields(indexFields), Arrays.asList(index.orders())));
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
