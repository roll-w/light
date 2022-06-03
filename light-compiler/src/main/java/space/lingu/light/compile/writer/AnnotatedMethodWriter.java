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

package space.lingu.light.compile.writer;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import space.lingu.light.compile.JavaPoetClass;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.struct.Field;
import space.lingu.light.compile.struct.Pojo;
import space.lingu.light.util.Pair;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author RollW
 */
public class AnnotatedMethodWriter {
    private final Pojo pojo;

    public AnnotatedMethodWriter(Pojo pojo) {
        this.pojo = pojo;
    }

    public MethodSpec createBindMethod(ClassWriter writer, List<Field> fields) {
        return createBindMethod(writer, fields, 0);
    }

    public MethodSpec createBindMethod(ClassWriter writer, List<Field> fields, int offset) {
        GenerateCodeBlock bindBlock = new GenerateCodeBlock(writer);
        MethodSpec.Builder bindMethodBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(TypeName.VOID)
                .addParameter(ParameterSpec
                        .builder(JavaPoetClass.JdbcNames.PREPARED_STMT, "stmt")
                        .build())
                .addParameter(ParameterSpec
                        .builder(pojo.getTypeName(), "value")
                        .build());

        List<Pair<Field, String>> pairList = new ArrayList<>();
        IntStream.range(0, fields.size()).forEach(value -> {
            Field field = fields.get(value);
            pairList.add(Pair.createPair(field, String.valueOf(value + 1 + offset)));
        });
        FieldReadWriteWriter.bindToStatement("value", "stmt", pairList, bindBlock);
        bindMethodBuilder.addCode(bindBlock.builder().build());
        return bindMethodBuilder.build();
    }
}
