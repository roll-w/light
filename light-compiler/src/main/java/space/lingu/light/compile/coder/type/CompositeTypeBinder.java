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

package space.lingu.light.compile.coder.type;

import com.squareup.javapoet.TypeName;
import space.lingu.light.compile.coder.ColumnTypeBinder;
import space.lingu.light.compile.coder.ColumnValueReader;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.StatementBinder;

import javax.lang.model.type.TypeMirror;

/**
 * @author RollW
 */
public class CompositeTypeBinder extends ColumnTypeBinder
        implements StatementBinder, ColumnValueReader {
    private final ColumnTypeBinder binder;
    private final TypeConverter stmtConverter;
    private final TypeConverter readConverter;

    public CompositeTypeBinder(TypeMirror type, ColumnTypeBinder binder,
                               TypeConverter inConverter,
                               TypeConverter outConverter) {
        super(type, binder.getDataType());
        this.binder = binder;
        this.stmtConverter = inConverter;
        this.readConverter = outConverter;
    }

    @Override
    public void readFromResultSet(String outVarName,
                                  String resultSetName,
                                  String indexName, GenerateCodeBlock block) {
        if (readConverter == null) {
            return;
        }
        final String tempVar = block.getTempVar();
        block.builder().addStatement("final $T $L",
                TypeName.get(binder.type()), tempVar);
        binder.readFromResultSet(tempVar, resultSetName, indexName, block);
        readConverter.convert(tempVar, outVarName, block);
    }

    @Override
    public void bindToStatement(String stmtVarName,
                                String indexVarName,
                                String valueVarName,
                                GenerateCodeBlock block) {
        if (stmtConverter == null) {
            return;
        }
        final String tempVar = block.getTempVar();
        block.builder().addStatement("final $T $L",
                TypeName.get(stmtConverter.to), tempVar);
        stmtConverter.convert(valueVarName, tempVar, block);
        binder.bindToStatement(stmtVarName, indexVarName, tempVar, block);
    }
}
