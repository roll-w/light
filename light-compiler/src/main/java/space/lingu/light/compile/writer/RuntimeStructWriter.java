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
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.struct.DataTable;
import space.lingu.light.compile.struct.Field;
import space.lingu.light.compile.struct.Index;
import space.lingu.light.compile.struct.PrimaryKey;
import space.lingu.light.struct.Table;
import space.lingu.light.struct.TableColumn;
import space.lingu.light.struct.TableIndex;
import space.lingu.light.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 转换为运行时可解析的对象结构<br>
 *
 * @author RollW
 * @see space.lingu.light.struct
 */
public class RuntimeStructWriter {
    private final DataTable mTable;

    public RuntimeStructWriter(DataTable dataTable) {
        mTable = dataTable;
    }

    /**
     * 将表映射到{@link space.lingu.light.struct.Table}
     *
     * @param block 代码块
     * @return 生成临时变量的名称
     */
    public String writeDataTable(GenerateCodeBlock block) {
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
        mTable.getIndices().forEach(index ->
                writeIndex(block, index, indexListVarName));
        mTable.getFields().forEach(field ->
                writeTableColumn(block, field, columnListVarName));

        block.builder()
                .addStatement("$T $L = new $T($S, $L, $L, $L)",
                        Table.class, tableVarName, Table.class,
                        mTable.getTableName(),
                        columnListVarName,
                        null,
                        indexListVarName
                );
        return tableVarName;
    }

    private void writeTableColumn(GenerateCodeBlock block, Field field, String listVarName) {
        final String tableColumnVarName = block.getTempVar("_tableColumn" + StringUtil.firstUpperCase(field.getName()));
        block.builder()
                .addStatement("$T $L = new $T($S, $S, $S, $T.$L)",
                        TableColumn.class, tableColumnVarName, TableColumn.class,
                        field.getColumnName(),
                        field.getName(),
                        field.getDefaultValue(),
                        SQLDataType.class, field.getDataType()
                )
                .addStatement("$L.add($L)", listVarName, tableColumnVarName);
    }

    private void writeIndex(GenerateCodeBlock block, Index index, String indexListVarName) {
        final String tableIndexVarName = block.getTempVar("_tableIndex" + StringUtil.firstUpperCase(index.getName()));
        block.builder()
                .addStatement("$T $L = new $T()", TableIndex.class, tableIndexVarName, TableIndex.class)
                .addStatement("$L.add($L)", indexListVarName, tableIndexVarName);

    }

    private void writePrimaryKey(GenerateCodeBlock block, PrimaryKey index, String columnsListVarName) {

    }

}
