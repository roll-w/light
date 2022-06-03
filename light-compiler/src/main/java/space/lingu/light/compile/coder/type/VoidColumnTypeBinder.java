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

import space.lingu.light.compile.coder.ColumnTypeBinder;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.javac.ProcessEnv;

import javax.lang.model.type.TypeKind;

/**
 * 处理返回值void
 * @author RollW
 */
public class VoidColumnTypeBinder extends ColumnTypeBinder {
    public VoidColumnTypeBinder() {
        super(null, null);
    }

    @Override
    public void readFromResultSet(String outVarName, String resultSetName, String indexName, GenerateCodeBlock block) {
        // no-op
    }

    @Override
    public void bindToStatement(String stmtVarName, String indexVarName, String valueVarName, GenerateCodeBlock block) {
        // no-op
    }
}
