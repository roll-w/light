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

import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.annotated.binder.AnnotatedMethodBinder;
import space.lingu.light.compile.struct.DeleteMethod;

/**
 * @author RollW
 */
public class HandlerDeleteResultBinder extends QueryResultBinder {
    private final AnnotatedMethodBinder binder;
    private final DeleteMethod method;

    public HandlerDeleteResultBinder(DeleteMethod method, AnnotatedMethodBinder binder) {
        super(null);
        this.binder = binder;
        this.method = method;
    }

    @Override
    public void writeBlock(String handlerName,
                           String stmtVarName,
                           boolean canReleaseSet,
                           boolean isReturn,
                           boolean inTransaction,
                           GenerateCodeBlock block) {
        throw new IllegalStateException("Cannot invoke this method.");
    }

    public AnnotatedMethodBinder getBinder() {
        return binder;
    }

    public DeleteMethod getMethod() {
        return method;
    }
}
