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

package space.lingu.light.compile.coder.custom.binder;

import com.squareup.javapoet.TypeName;
import space.lingu.light.compile.coder.GenerateCodeBlock;

/**
 * @author RollW
 */
public class DeleteResultBinder extends QueryResultBinder {
    public DeleteResultBinder() {
        super(null);
    }

    public static DeleteResultBinder getInstance() {
        return Singleton.INSTANCE;
    }

    @Override
    public void writeBlock(String handlerName,
                           String stmtVarName,
                           boolean canReleaseSet,
                           boolean isReturn,
                           boolean inTransaction,
                           GenerateCodeBlock block) {
        if (inTransaction) {
            block.builder().addStatement("$N.beginTransaction()", handlerName);
        }
        final String outVar = block.getTempVar("_result");
        block.builder()
                .beginControlFlow("try")
                .addStatement("$T $L = $N.executeUpdate()",
                        TypeName.INT, outVar, stmtVarName);
        end(handlerName, stmtVarName, outVar,
                canReleaseSet, isReturn,
                inTransaction, block);
    }

    private static final class Singleton {
        static final DeleteResultBinder INSTANCE = new DeleteResultBinder();
    }
}
