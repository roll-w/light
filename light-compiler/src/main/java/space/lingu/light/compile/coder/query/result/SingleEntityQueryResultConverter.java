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

package space.lingu.light.compile.coder.query.result;

import com.squareup.javapoet.ClassName;
import space.lingu.light.LightRuntimeException;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.query.row.RowConverter;

import javax.lang.model.type.TypeMirror;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * 只有一个实体返回值
 * @author RollW
 */
public class SingleEntityQueryResultConverter extends QueryResultConverter {
    private final RowConverter mConverter;

    public SingleEntityQueryResultConverter(RowConverter converter) {
        super(Collections.singletonList(converter));
        mConverter = converter;
    }

    @Override
    public void convert(String outVarName, String resultSetName, GenerateCodeBlock block) {
        mConverter.onResultSetReady(resultSetName, block);
        block.builder().addStatement("final $T $L", ClassName.get(mConverter.getOutType()), outVarName)
                .beginControlFlow("if ($L.first())", resultSetName);
        mConverter.convert(outVarName, resultSetName, block);
        block.builder().nextControlFlow("else")
                .addStatement("$L = $L", outVarName, getDefaultValue(mConverter.getOutType()))
                .endControlFlow();
        mConverter.onResultSetFinish(block);
    }

    private String getDefaultValue(TypeMirror typeMirror) {
        switch (typeMirror.getKind()) {
            case BOOLEAN:
                return "false";
            case LONG:
                return "0L";
            case FLOAT:
                return "0F";
            case DOUBLE:
                return "0.0";
            case CHAR:
            case INT:
            case BYTE:
            case SHORT:
                return "0";
            default: return null;
        }
    }
}
