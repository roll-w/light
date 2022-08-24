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

package space.lingu.light.compile.coder.custom.row;

import com.squareup.javapoet.MethodSpec;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.struct.DataTable;
import space.lingu.light.compile.writer.DataTableResultSetConverterWriter;

/**
 * @author RollW
 */
public class DataTableRowConverter extends RowConverter {
    private final DataTable mTable;
    private MethodSpec methodSpec;

    protected DataTableRowConverter(DataTable table) {
        super(table.getElement().asType());
        mTable = table;
    }

    @Override
    public void onResultSetReady(String resultSetName, GenerateCodeBlock block) {
        methodSpec = block.writer.getOrCreateMethod(
                new DataTableResultSetConverterWriter(mTable));
    }

    @Override
    public void convert(String outVarName, String resultSetName, GenerateCodeBlock block) {
        block.builder().addStatement("$L = $N($L)", outVarName, methodSpec, resultSetName);
    }

    @Override
    public void onResultSetFinish(GenerateCodeBlock block) {
        //
    }
}
