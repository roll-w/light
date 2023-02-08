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

import com.squareup.javapoet.TypeName;
import space.lingu.light.compile.JavaPoetClass;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.struct.Field;
import space.lingu.light.compile.struct.Pojo;
import space.lingu.light.compile.writer.FieldReadWriteWriter;
import space.lingu.light.util.StringUtil;

import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/**
 * @author RollW
 */
public class PojoRowConverter extends RowConverter {
    private final Pojo mPojo;
    private final List<Field> usedFields = new ArrayList<>();
    private final List<FieldReadWriteWriter.FieldWithNumber> fieldWithNumberList = new ArrayList<>();

    public PojoRowConverter(Pojo pojo, TypeMirror outType) {
        super(outType);
        mPojo = pojo;
        usedFields.addAll(mPojo.getFields().getFields());
    }

    @Override
    public void onResultSetReady(String resultSetName, GenerateCodeBlock block) {
        usedFields.forEach(field -> {
            final String numberVar = block.getTempVar("_resultSetIndexOf" +
                    StringUtil.firstUpperCase(field.getName()));
            block.builder().addStatement("final $T $L = $T.getColumnIndexSwallow($L, $S)",
                    TypeName.INT, numberVar, JavaPoetClass.UtilNames.RESULT_SET_UTIL,
                    resultSetName, field.getColumnName());
            fieldWithNumberList.add(new FieldReadWriteWriter.FieldWithNumber(field, numberVar));
        });
    }

    @Override
    public void convert(String outVarName, String resultSetName, GenerateCodeBlock block) {
        FieldReadWriteWriter.readFromResultSet(outVarName, mPojo, resultSetName, fieldWithNumberList, block);
    }

    @Override
    public void onResultSetFinish(GenerateCodeBlock block) {

    }
}
