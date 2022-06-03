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

package space.lingu.light.compile.struct;

import javax.lang.model.element.TypeElement;

/**
 * 主键结构
 * @author RollW
 */
public class PrimaryKey {
    private final TypeElement declaredIn;
    private final Field.Fields fields;
    private final boolean autoGenerate;

    public PrimaryKey(TypeElement declaredIn, Field.Fields fields, boolean autoGenerate) {
        this.declaredIn = declaredIn;
        this.fields = fields;
        this.autoGenerate = autoGenerate;
    }

    public static final PrimaryKey MISSING = new PrimaryKey(null, new Field.Fields(), false);

    public TypeElement getDeclaredIn() {
        return declaredIn;
    }

    public Field.Fields getFields() {
        return fields;
    }

    public boolean isAutoGenerate() {
        return autoGenerate;
    }
}
