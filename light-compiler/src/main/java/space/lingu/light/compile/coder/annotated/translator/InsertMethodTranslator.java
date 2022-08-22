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
import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeUtil;
import space.lingu.light.compile.struct.AnnotateParameter;
import space.lingu.light.util.Pair;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 将给定Insert操作翻译成Java代码
 * @author RollW
 */
public class InsertMethodTranslator {
    private final InsertType insertType;

    public static InsertMethodTranslator create(ExecutableElement methodElement,
                                                ProcessEnv env,
                                                List<AnnotateParameter> params) {
        TypeMirror returnType = methodElement.getReturnType();
        InsertType insertType = getInsertType((TypeElement) env.getTypeUtils().asElement(returnType), returnType);
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

    private static boolean checkType(InsertType type, List<AnnotateParameter> params) {
        if (type == null) {
            return false;
        }
        if (params.size() != 1) {
            return type == InsertType.VOID || type == InsertType.VOID_OBJECT;
        }
        if (params.get(0).isMultiple()) {
            return sMultipleList.contains(type);
        }
        return type == InsertType.VOID ||
                type == InsertType.VOID_OBJECT ||
                type == InsertType.SINGLE_ID;
    }

    private static final List<InsertType> sMultipleList = new ArrayList<>();
    static {
        sMultipleList.add(InsertType.VOID);
        sMultipleList.add(InsertType.VOID_OBJECT);
        sMultipleList.add(InsertType.ID_ARRAY);
        sMultipleList.add(InsertType.ID_ARRAY_BOXED);
        sMultipleList.add(InsertType.ID_LIST);
    }

    private static InsertType getInsertType(TypeElement typeElement,
                                            TypeMirror typeMirror) {
        if (typeElement == null) {
            if (typeMirror.getKind() == TypeKind.LONG) {
                return InsertType.SINGLE_ID;
            }
            if (typeMirror.getKind() == TypeKind.VOID) {
                return InsertType.VOID;
            }
            if (typeMirror.getKind() == TypeKind.ARRAY) {
                TypeMirror arrayType = TypeUtil.getArrayElementType(typeMirror);
                if (TypeUtil.isLong(arrayType)) {
                    return InsertType.ID_ARRAY;
                }
                if (ElementUtil.isLongBoxed(ElementUtil.asTypeElement(arrayType))) {
                    return InsertType.ID_ARRAY_BOXED;
                }
            }
            return null;
        }

        if (ElementUtil.isLong(typeElement)) {
            return InsertType.SINGLE_ID;
        }

        if (ElementUtil.isList(typeElement)) {
            if (ElementUtil.isLong(ElementUtil.getGenericElements(typeMirror).get(0))) {
                return InsertType.ID_LIST;
            }
        }

        return null;
    }

    public void createMethodBody(List<AnnotateParameter> params,
                                 Map<String, Pair<FieldSpec, TypeSpec>> insertHandlers,
                                 GenerateCodeBlock block) {
        boolean needsReturn = insertType != InsertType.VOID &&
                insertType != InsertType.VOID_OBJECT;
        final String returnVarName = needsReturn ? block.getTempVar("_result") : null;

        /* 示例，产生类似如下代码
            try {
              __insertHandlerOfItem.beginTransaction();
              long _result = __insertHandlerOfItem.insertAndReturnId(item);
              return _result;
            } finally {
              __insertHandlerOfItem.endTransaction();
            }
        */
        params.forEach(param -> {
            FieldSpec insertHandlerField = insertHandlers.get(param.getName()).first;
            block.builder().beginControlFlow("try")
                    .addStatement("$N.beginTransaction()", insertHandlerField);
            if (needsReturn) {
                block.builder().addStatement("$T $L = $N.$L($L)", insertType.returnType, returnVarName,
                        insertHandlerField, insertType.methodName, param.getName());
            } else {
                block.builder().addStatement("$N.$L($L)", insertHandlerField, insertType.methodName, param.getName());
            }

            if (needsReturn) {
                block.builder().addStatement("return $L", returnVarName);
            } else if (insertType == InsertType.VOID_OBJECT) {
                block.builder().addStatement("return null");
            }

            block.builder().nextControlFlow("finally")
                    .addStatement("$N.endTransaction()", insertHandlerField)
                    .endControlFlow();
        });
    }

    private InsertMethodTranslator(InsertType insertType) {
        this.insertType = insertType;
    }

    /**
     * 返回值类型
     */
    public enum InsertType {
        // 修改space.lingu.light.handler.InsertHandler下的方法名时，这里也要对应修改
        /**return void*/
        VOID("insert", TypeName.VOID),
        /**return void*/
        VOID_OBJECT("insert", TypeName.VOID.box()),
        /**return long/Long*/
        SINGLE_ID("insertAndReturnId", TypeName.LONG),
        /**return long[]*/
        ID_ARRAY("insertAndReturnIdsArray", ArrayTypeName.of(TypeName.LONG)),
        /**return Long[]*/
        ID_ARRAY_BOXED("insertAndReturnIdsArrayBox",
                ArrayTypeName.of(TypeName.LONG.box())),
        /**return List&lt;Long&gt;*/
        ID_LIST("insertAndReturnIdsList",
                ParameterizedTypeName.get(ClassName.get(List.class), TypeName.LONG.box()));
        public final String methodName;
        public final TypeName returnType;

        InsertType(String methodName, TypeName returnType) {
            this.methodName = methodName;
            this.returnType = returnType;
        }
    }
}
