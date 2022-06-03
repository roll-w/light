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

package space.lingu.light.compile.coder.annotated.binder;

import com.squareup.javapoet.ClassName;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.annotated.translator.TransactionMethodTranslator;
import space.lingu.light.compile.javac.TypeUtil;

import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * @author RollW
 */
public class DirectTransactionMethodBinder extends TransactionMethodBinder {
    public DirectTransactionMethodBinder(TransactionMethodTranslator translator) {
        super(translator);
    }

    @Override
    public void writeBlock(TypeMirror returnType, List<String> params,
                           ClassName dao, ClassName daoImpl, GenerateCodeBlock block) {
        final String resultVar;
        boolean returnsValue = !TypeUtil.isVoid(returnType);
        if (returnsValue) {
            resultVar = block.getTempVar("_result");
        }  else {
            resultVar = null;
        }

        block.builder().beginControlFlow("try");
        mTranslator.createMethodBody(returnType, params, dao, daoImpl, resultVar, block);
        if (returnsValue) {
            block.builder().addStatement("return $N", resultVar);
        }
        block.builder().nextControlFlow("finally")
                .endControlFlow();
    }
}
