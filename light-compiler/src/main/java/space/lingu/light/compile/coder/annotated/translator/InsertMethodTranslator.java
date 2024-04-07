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

import com.squareup.javapoet.*;
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.javac.ElementUtils;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeUtils;
import space.lingu.light.compile.processor.ReturnTypes;
import space.lingu.light.compile.struct.Parameter;
import space.lingu.light.util.Pair;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Translator for insert method.
 *
 * @author RollW
 */
public class InsertMethodTranslator {
    private final InsertType insertType;

    public static InsertMethodTranslator create(ExecutableElement methodElement,
                                                ProcessEnv env,
                                                List<Parameter> params) {
        TypeMirror returnType = methodElement.getReturnType();
        InsertType insertType = getInsertType(
                env,
                (TypeElement) env.getTypeUtils().asElement(returnType),
                returnType
        );
        if (insertType == null) {
            env.getLog().error(
                    CompileErrors.INSERT_RETURN_TYPE,
                    methodElement
            );
        }
        if (checkType(insertType, params)) {
            return new InsertMethodTranslator(insertType);
        }
        env.getLog().error(
                CompileErrors.INSERT_RETURN_TYPE_NOT_MATCHED,
                methodElement
        );

        return null;
    }

    private static boolean checkType(InsertType type, List<Parameter> params) {
        if (type == null) {
            return false;
        }
        if (params.size() != 1) {
            return type == InsertType.VOID || type == InsertType.VOID_OBJECT;
        }
        if (params.get(0).isMultiple()) {
            return MULTIPLE_LIST.contains(type);
        }
        return type == InsertType.VOID || type == InsertType.VOID_OBJECT ||
                type == InsertType.LONG || type == InsertType.INT;
    }

    private static final List<InsertType> MULTIPLE_LIST = new ArrayList<>();

    static {
        MULTIPLE_LIST.add(InsertType.VOID);
        MULTIPLE_LIST.add(InsertType.VOID_OBJECT);
        MULTIPLE_LIST.add(InsertType.LONG_ARRAY);
        MULTIPLE_LIST.add(InsertType.LONG_ARRAY_BOXED);
        MULTIPLE_LIST.add(InsertType.LONG_LIST);
        MULTIPLE_LIST.add(InsertType.INT_ARRAY);
        MULTIPLE_LIST.add(InsertType.INT_ARRAY_BOXED);
        MULTIPLE_LIST.add(InsertType.INT_LIST);
    }

    private static InsertType getInsertType(ProcessEnv env,
                                            TypeElement typeElement,
                                            TypeMirror typeMirror) {
        if (typeElement == null) {
            if (typeMirror.getKind() == TypeKind.LONG) {
                return InsertType.LONG;
            }
            if (typeMirror.getKind() == TypeKind.INT) {
                return InsertType.INT;
            }
            if (typeMirror.getKind() == TypeKind.VOID) {
                return InsertType.VOID;
            }
            if (typeMirror.getKind() == TypeKind.ARRAY) {
                return getArrayInsertType(typeMirror);
            }
            return null;
        }

        if (ElementUtils.isLong(typeElement)) {
            return InsertType.LONG;
        }

        if (ElementUtils.isInt(typeElement)) {
            return InsertType.INT;
        }

        if (ReturnTypes.isLegalCollectionReturnType(typeElement)) {
            return getCollectionInsertType(typeMirror);
        }

        return null;
    }

    private static InsertType getCollectionInsertType(TypeMirror typeMirror) {
        List<TypeElement> genericElements =
                ElementUtils.getGenericElements(typeMirror);
        TypeElement genericType = genericElements.get(0);

        if (ElementUtils.isLong(genericType)) {
            return InsertType.LONG_LIST;
        }
        if (ElementUtils.isInt(genericType)) {
            return InsertType.INT_LIST;
        }
        return null;
    }

    private static InsertType getArrayInsertType(TypeMirror typeMirror) {
        TypeMirror arrayType = TypeUtils.getArrayElementType(typeMirror);
        TypeElement arrayElement = ElementUtils.asTypeElement(arrayType);
        if (TypeUtils.isLong(arrayType)) {
            return InsertType.LONG_ARRAY;
        }
        if (ElementUtils.isLongBoxed(arrayElement)) {
            return InsertType.LONG_ARRAY_BOXED;
        }
        if (TypeUtils.isInt(arrayType)) {
            return InsertType.INT_ARRAY;
        }
        if (ElementUtils.isIntBoxed(arrayElement)) {
            return InsertType.INT_ARRAY_BOXED;
        }
        return null;
    }

    public void createMethodBody(List<Parameter> params,
                                 Map<String, Pair<FieldSpec, TypeSpec>> insertHandlers,
                                 GenerateCodeBlock block) {
        boolean needsReturn = insertType != InsertType.VOID &&
                insertType != InsertType.VOID_OBJECT;
        final String returnVarName = needsReturn ? block.getTempVar("_result") : null;

        params.forEach(param -> {
            FieldSpec insertHandlerField = insertHandlers.get(param.getName()).first;

            // now we don't need to manually open the transaction,
            // the handler will do it for us.
            if (needsReturn) {
                block.builder().addStatement("$T $L = $N.$L($L)",
                        insertType.getReturnType(), returnVarName,
                        insertHandlerField,
                        insertType.getMethodName(), param.getName()
                );
            } else {
                block.builder().addStatement("$N.$L($L)",
                        insertHandlerField,
                        insertType.getMethodName(),
                        param.getName()
                );
            }

            if (needsReturn) {
                block.builder().addStatement("return $L", returnVarName);
            } else if (insertType == InsertType.VOID_OBJECT) {
                block.builder().addStatement("return null");
            }
        });
    }

    private InsertMethodTranslator(InsertType insertType) {
        this.insertType = insertType;
    }

    /**
     * Return types.
     *
     * @see space.lingu.light.handler.InsertHandler
     */
    public enum InsertType {
        /**
         * return void
         */
        VOID("insert", TypeName.VOID),

        /**
         * return void
         */
        VOID_OBJECT("insert", TypeName.VOID.box()),

        /**
         * return long/Long
         */
        LONG("insertAndReturnLong", TypeName.LONG),

        /**
         * return long[]
         */
        LONG_ARRAY("insertAndReturnLongArray", ArrayTypeName.of(TypeName.LONG)),

        /**
         * return Long[]
         */
        LONG_ARRAY_BOXED("insertAndReturnLongArrayBox",
                ArrayTypeName.of(TypeName.LONG.box())),

        /**
         * return {@code List<Long>}
         */
        LONG_LIST("insertAndReturnLongList",
                ParameterizedTypeName.get(ClassName.get(List.class), TypeName.LONG.box())),

        /**
         * return int/Integer
         */
        INT("insertAndReturnInt", TypeName.INT),

        /**
         * return int[]
         */
        INT_ARRAY("insertAndReturnIntArray", ArrayTypeName.of(TypeName.INT)),

        /**
         * return Integer[]
         */
        INT_ARRAY_BOXED("insertAndReturnIntArrayBox",
                ArrayTypeName.of(TypeName.INT.box())),

        /**
         * return {@code List<Integer>}
         */
        INT_LIST("insertAndReturnIntList",
                ParameterizedTypeName.get(ClassName.get(List.class), TypeName.INT.box()));

        private final String methodName;
        private final TypeName returnType;

        InsertType(String methodName, TypeName returnType) {
            this.methodName = methodName;
            this.returnType = returnType;
        }

        public String getMethodName() {
            return methodName;
        }

        public TypeName getReturnType() {
            return returnType;
        }
    }
}
