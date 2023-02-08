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

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.javac.TypeUtil;
import space.lingu.light.compile.struct.Parameter;
import space.lingu.light.util.Pair;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;

/**
 * @author RollW
 */
public class AutoDeleteUpdateMethodTranslator {
    private final TypeMirror mReturnType;

    private AutoDeleteUpdateMethodTranslator(TypeMirror mirror) {
        mReturnType = mirror;
    }

    public static AutoDeleteUpdateMethodTranslator create(TypeMirror typeMirror,
                                                          List<Parameter> params) {
        if (check(typeMirror, params)) {
            return new AutoDeleteUpdateMethodTranslator(typeMirror);
        }
        return null;
    }


    public void createMethodBody(List<Parameter> params,
                                 Map<String, Pair<FieldSpec, TypeSpec>> handlers,
                                 GenerateCodeBlock block) {
        boolean returnsInt = isReturnInt(mReturnType);
        boolean returnsNull = isReturnNull(mReturnType);
        final String returnVarName = returnsInt ? block.getTempVar("_resultTotal") : null;
        final String methodName = handlerMethodName(params.get(0).isMultiple());

        if (returnVarName != null) {
            block.builder().addStatement("$T $L = 0", TypeName.INT, returnVarName);
        }

        params.forEach(param -> {
            FieldSpec handlerField = handlers.get(param.getName()).first;

            String increaseVar;
            if (returnVarName == null) {
                increaseVar = "";
            } else {
                increaseVar = returnVarName + " += ";
            }

            block.builder().addStatement("$L$N.$L($L)",
                    increaseVar, handlerField, methodName, param.getName());
        });

        if (returnVarName != null) {
            block.builder().addStatement("return $L", returnVarName);
        } else if (returnsNull) {
            block.builder().addStatement("return null");
        }

    }

    private String handlerMethodName(boolean isMultiple) {
        return isMultiple ? "handleMultiple" : "handle";
    }

    protected static boolean check(TypeMirror mirror, List<Parameter> params) {
        if (mirror == null) {
            return false;
        }
        if (params.isEmpty()) {
            return isReturnVoid(mirror) || isReturnNull(mirror);
        }

        return isValidReturn(mirror);
    }

    protected static boolean isValidReturn(TypeMirror returnType) {
        return isReturnVoid(returnType) ||
                isReturnNull(returnType) ||
                isReturnInt(returnType);
    }

    protected static boolean isReturnVoid(TypeMirror returnType) {
        return TypeUtil.isVoid(returnType);
    }

    protected static boolean isReturnNull(TypeMirror returnType) {
        return TypeName.get(returnType).equals(TypeName.VOID.box());
    }

    protected static boolean isReturnInt(TypeMirror returnType) {
        return TypeUtil.isInt(returnType) ||
                TypeName.get(returnType).equals(TypeName.INT.box());
    }

}
