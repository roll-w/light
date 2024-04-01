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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import space.lingu.light.LightRuntimeException;
import space.lingu.light.SQLDataType;
import space.lingu.light.compile.JavaPoetClass;
import space.lingu.light.compile.coder.ColumnTypeBinder;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.writer.ClassWriter;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * @author RollW
 */
public class EnumColumnTypeBinder extends ColumnTypeBinder {
    private final TypeElement type;
    private final List<VariableElement> enumConstants;

    public EnumColumnTypeBinder(TypeCompileType type) {
        super(type, SQLDataType.VARCHAR);
        if (type.getElement() == null ||
                type.getElement().getKind() != ElementKind.ENUM) {
            throw new IllegalArgumentException("Not an enum kind.");
        }
        this.type = type.getElement();
        this.enumConstants = getEnumConstants();
    }

    private List<VariableElement> getEnumConstants() {
        List<VariableElement> elements = new ArrayList<>();
        type.getEnclosedElements().forEach(element -> {
            if (element.getKind() == ElementKind.ENUM_CONSTANT) {
                elements.add((VariableElement) element);
            }
        });
        return elements;
    }

    @Override
    public void readFromResultSet(String outVarName,
                                  String resultSetName,
                                  String indexName,
                                  GenerateCodeBlock block) {
        MethodSpec stringToEnumMethod = stringToEnumMethod(block);
        boolean needCheckIndex = IndexHelper.isNeedCheckIndex(indexName);
        if (needCheckIndex) {
            block.builder()
                    .beginControlFlow("if ($L < 0)", indexName)
                    .addStatement("$L = null", outVarName)
                    .nextControlFlow("else");
        }
        block.builder().addStatement("$L = $N($L.getString($L))",
                        outVarName, stringToEnumMethod, resultSetName, indexName);
        if (needCheckIndex) {
            block.builder().endControlFlow();
        }
    }

    @Override
    public void bindToStatement(String stmtVarName,
                                String indexVarName,
                                String valueVarName,
                                GenerateCodeBlock block) {
        MethodSpec enumToStringMethod = enumToStringMethod(block);
        block.builder()
                .beginControlFlow("try")
                .beginControlFlow("if ($L == null)", valueVarName)
                .addStatement("$L.setNull($L, $L)", stmtVarName, indexVarName, Types.NULL)
                .nextControlFlow("else")
                .addStatement("$L.setString($L, $N($L))", stmtVarName, indexVarName,
                        enumToStringMethod, valueVarName)
                .endControlFlow()
                .nextControlFlow("catch ($T e)", SQLException.class)
                .addStatement("throw new $T(e)", LightRuntimeException.class)
                .endControlFlow();
    }

    private MethodSpec stringToEnumMethod(GenerateCodeBlock block) {
        return block.writer.getOrCreateMethod(new ClassWriter.SharedMethodSpec(type.getSimpleName() + "_StringToEnum") {
            @Override
            protected String getUniqueKey() {
                return "stringToEnum_" + ClassName.get(type);
            }

            @Override
            protected void prepare(String methodName, ClassWriter writer, MethodSpec.Builder builder) {
                ClassName className = ClassName.get(type);
                ParameterSpec parameter = ParameterSpec
                        .builder(JavaPoetClass.LangNames.STRING, "_value", Modifier.FINAL)
                        .build();
                builder.addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                        .returns(className)
                        .addParameter(parameter)
                        .beginControlFlow("if ($N == null)", parameter)
                        .addStatement("return null")
                        .endControlFlow()
                        .beginControlFlow("switch ($N)", parameter);
                enumConstants.forEach(variableElement ->
                        builder.addStatement("case $S: return $T.$L",
                                variableElement.getSimpleName().toString(),
                                className,
                                variableElement.getSimpleName().toString()));
                builder.addStatement("default: throw new $T($S + $N)",
                                IllegalArgumentException.class,
                                "Can't convert value to enum, unknown value: ", parameter)
                        .endControlFlow();
            }
        });
    }

    private MethodSpec enumToStringMethod(GenerateCodeBlock block) {
        return block.writer.getOrCreateMethod(new ClassWriter.SharedMethodSpec(type.getSimpleName() + "_EnumToString") {
            @Override
            protected String getUniqueKey() {
                return "enumToString_" + ClassName.get(type);
            }

            @Override
            protected void prepare(String methodName, ClassWriter writer, MethodSpec.Builder builder) {
                ClassName className = ClassName.get(type);
                ParameterSpec parameter = ParameterSpec
                        .builder(className, "_value", Modifier.FINAL)
                        .build();
                builder.addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                        .returns(String.class)
                        .addParameter(parameter)
                        .beginControlFlow("if ($N == null)", parameter)
                        .addStatement("return null")
                        .endControlFlow()
                        .beginControlFlow("switch ($N)", parameter);
                enumConstants.forEach(variableElement ->
                        builder.addStatement("case $L: return $S",
                                variableElement.getSimpleName().toString(),
                                variableElement.getSimpleName().toString()));
                builder.addStatement("default: throw new $T($S + $N)",
                                IllegalArgumentException.class,
                                "Can't convert value to enum, unknown value: ", parameter)
                        .endControlFlow();
            }
        });
    }
}
