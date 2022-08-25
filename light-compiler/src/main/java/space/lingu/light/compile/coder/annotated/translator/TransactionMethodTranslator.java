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

package space.lingu.light.compile.coder.annotated.translator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.struct.TransactionMethod;

import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author RollW
 */
public class TransactionMethodTranslator {
    private final String methodName;
    private final TransactionMethod.CallType callType;

    public TransactionMethodTranslator(String methodName, TransactionMethod.CallType callType) {
        this.methodName = methodName;
        this.callType = callType;
    }

    public void createMethodBody(TypeMirror returnType,
                                 List<String> paramNames,
                                 ClassName dao,
                                 ClassName daoImpl,
                                 String resultVar,
                                 GenerateCodeBlock block) {
        List<Object> params = new ArrayList<>();
        StringBuilder format = new StringBuilder();
        if (resultVar != null) {
            format.append("$T $L = ");
            params.add(TypeName.get(returnType));
            params.add(resultVar);
        }
        switch (callType) {
            case DIRECT:
            case INHERITED_DEFAULT:
                format.append("$T.super.$N(");
                params.add(daoImpl);
                params.add(methodName);
                break;
            case DEFAULT:
                format.append("$T.super.$N(");
                params.add(dao);
                params.add(methodName);
                break;
        }
        AtomicBoolean first = new AtomicBoolean(true);
        paramNames.forEach(s -> {
            if (first.get()) {
                first.set(false);
            } else {
                format.append(", ");
            }
            format.append("$L");
            params.add(s);
        });
        format.append(")");
        block.builder().addStatement(format.toString(), params.toArray());
    }
}
