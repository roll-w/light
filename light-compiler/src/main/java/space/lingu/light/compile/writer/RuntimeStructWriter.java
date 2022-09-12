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

package space.lingu.light.compile.writer;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import space.lingu.light.SQLDataType;
import space.lingu.light.compile.JavaPoetClass;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.struct.*;
import space.lingu.light.struct.Table;
import space.lingu.light.struct.TableColumn;
import space.lingu.light.struct.TableIndex;
import space.lingu.light.struct.TablePrimaryKey;
import space.lingu.light.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Convert to runtime struct.
 *
 * @author RollW
 * @see space.lingu.light.struct
 */
public class RuntimeStructWriter {
    private final DataTable mTable;

    public RuntimeStructWriter(DataTable dataTable) {
        mTable = dataTable;
    }

    public String writeDatabase(GenerateCodeBlock block) {
        return null;
    }

    /**
     * 将表映射到{@link space.lingu.light.struct.Table}
     *
     * @param block 代码块
     * @return 生成临时变量的名称
     */
    public String writeDataTable(GenerateCodeBlock block, String databaseConfVarName) {
        String classSimpleName = ((ClassName) mTable.getTypeName()).simpleName();
        final String tableVarName = block.getTempVar("_tableOf" + StringUtil.firstUpperCase(classSimpleName));
        final String columnListVarName = block.getTempVar("_columnListOf" + StringUtil.firstUpperCase(classSimpleName));
        final String indexListVarName = block.getTempVar("_indexListOf" + StringUtil.firstUpperCase(classSimpleName));
        TypeName columnListType = ParameterizedTypeName
                .get(ClassName.get(List.class), ClassName.get(TableColumn.class));
        TypeName columnArrayListType = ParameterizedTypeName
                .get(ClassName.get(ArrayList.class), ClassName.get(TableColumn.class));

        TypeName indexListType = ParameterizedTypeName
                .get(ClassName.get(List.class), ClassName.get(TableIndex.class));
        TypeName indexArrayListType = ParameterizedTypeName
                .get(ClassName.get(ArrayList.class), ClassName.get(TableIndex.class));

        block.builder()
                .addStatement("$T $L = new $T()", columnListType, columnListVarName, columnArrayListType)
                .addStatement("$T $L = new $T()", indexListType, indexListVarName, indexArrayListType);
        String tableConfVarName = writeConfigurationsAndFork(mTable,
                "TbOf" + mTable.getElement().getSimpleName().toString(),
                databaseConfVarName, block);
        mTable.getIndices().forEach(index ->
                writeIndex(block, index, indexListVarName));
        mTable.getFields().forEach(field ->
                writeTableColumn(block, field, columnListVarName, tableConfVarName));
        final String pkVarName = writePrimaryKey(block, mTable.getPrimaryKey());

        block.builder()
                .addStatement("$T $L = new $T($S, $L, $L, $L, $L)",
                        Table.class, tableVarName, Table.class,
                        mTable.getTableName(),
                        columnListVarName,
                        pkVarName,
                        indexListVarName,
                        tableConfVarName
                );
        return tableVarName;
    }

    private String writeConfigurationsAndFork(Configurable configurable,
                                              String prefix,
                                              String prevConfVarName,
                                              GenerateCodeBlock block) {
        final String forkVarName = block.getTempVar("_configurationsFork" + prefix);
        String tempConf = Configurable.writeConfiguration(configurable, prefix, block);
        block.builder().addStatement("$T $L = $L.plus($L)",
                JavaPoetClass.CONFIGURATIONS,
                forkVarName, prevConfVarName, tempConf);
        return forkVarName;
    }

    private void writeTableColumn(GenerateCodeBlock block, Field field,
                                  String listVarName,
                                  String tableConfVarName) {
        final String tableColumnVarName = block.getTempVar("_tableColumn" + StringUtil.firstUpperCase(field.getName()));
        boolean autoGen = false;
        if (mTable.getPrimaryKey().getFields().hasField(field)) {
            autoGen = mTable.getPrimaryKey().isAutoGenerate();
        }
        String fieldConfVarName = writeConfigurationsAndFork(field,
                "ColumnOf" + StringUtil.firstUpperCase(field.getName()),
                tableConfVarName,
                block);
        block.builder()
                // Params:

                // String name, String fieldName,
                // String defaultValue,
                // boolean hasDefaultValue,
                // SQLDataType dataType,
                // boolean nullable,
                // boolean autoGenerate,
                // Configurations
                .addStatement(
                        "$T $L = new $T($S, $S, $S, $L,\n$T.$L, $L, $L, $L)",
                        TableColumn.class, tableColumnVarName, TableColumn.class,
                        // params
                        field.getColumnName(),
                        field.getName(),
                        field.getDefaultValue(),
                        field.isHasDefault(),
                        SQLDataType.class, field.getDataType(),
                        field.getNullability() != Nullability.NONNULL,
                        autoGen,
                        fieldConfVarName
                )
                .addStatement("$L.add($L)", listVarName, tableColumnVarName);
    }

    private void writeIndex(GenerateCodeBlock block, Index index, String indexListVarName) {
        final String tableIndexVarName = block.getTempVar("_tableIndex" + StringUtil.firstUpperCase(index.getName()));
        block.builder()
                .addStatement("$T $L = new $T($S, $L, $L, $L)",
                        TableIndex.class, tableIndexVarName, TableIndex.class,
                        index.getName(), index.isUnique(), null, null
                )
                .addStatement("$L.add($L)", indexListVarName, tableIndexVarName);

    }

    private String writePrimaryKey(GenerateCodeBlock block, PrimaryKey key) {
        final String primaryKeyVarName = block.getTempVar("_pkOf" + mTable.getTableName());
        for (Field field : key.getFields().fields) {

        }
        String pkColumnsVarName = block.getTempVar("_pkColumns");

        block.builder()
                .addStatement("$T $L = new $T($L, $L)",
                        TablePrimaryKey.class, primaryKeyVarName,
                        TablePrimaryKey.class,
                        null, key.isAutoGenerate()
                );

        return primaryKeyVarName;
    }

}
