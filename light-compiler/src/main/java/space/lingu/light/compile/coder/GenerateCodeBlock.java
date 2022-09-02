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

package space.lingu.light.compile.coder;

import com.squareup.javapoet.CodeBlock;
import space.lingu.light.compile.writer.ClassWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * 将临时变量限制在一个代码块内。
 * @author RollW
 */
public class GenerateCodeBlock {
    public static final String TMP_VAR_PREFIX = "_tmp";
    public static final String CLASS_PROPERTY_PREFIX = ClassWriter.CLASS_MEMBER_PREFIX;

    public final ClassWriter writer;

    private final Map<String, Integer> tempVars = new HashMap<>();
    private CodeBlock.Builder builder;

    public GenerateCodeBlock(ClassWriter writer) {
        this.writer = writer;
    }

    public CodeBlock.Builder builder() {
        if (builder == null) {
            builder = CodeBlock.builder();
        }
        return builder;
    }

    public String getTempVar() {
        return getTempVar(TMP_VAR_PREFIX);
    }

    public String getTempVar(String prefix) {
        if (!prefix.startsWith("_")) {
            throw new IllegalArgumentException("temp variable prefixes should start with _");
        }
        if (prefix.startsWith(CLASS_PROPERTY_PREFIX)) {
            throw new IllegalArgumentException("cannot use " + CLASS_PROPERTY_PREFIX + " for tmp variables");
        }
        int idx = tempVars.getOrDefault(prefix, 0);
        String res = generateTmpVarName(prefix, idx);
        tempVars.put(res, idx + 1);
        return res;
    }

    public CodeBlock generate() {
        return builder.build();
    }

    public GenerateCodeBlock copy() {
        GenerateCodeBlock block = new GenerateCodeBlock(writer);
        block.tempVars.putAll(tempVars);
        return block;
    }

    public static String generateTmpVarName(String prefix, int idx) {
        if (idx == 0) return prefix;
        return prefix + "_" + idx;
    }
}
