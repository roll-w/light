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

package space.lingu.light.compile.javac;

import com.google.auto.common.MoreTypes;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.SimpleTypeVisitor7;
import javax.lang.model.util.Types;

/**
 * @author RollW
 */
public class TypeUtil {
    public static boolean isAssignedFrom(Types util, TypeMirror f, TypeMirror s) {
        return util.isAssignable(util.erasure(f), s);
    }

    public static ExecutableType asExecutable(TypeMirror typeMirror) {
        return MoreTypes.asExecutable(typeMirror);
    }

    public static DeclaredType asDeclared(TypeMirror typeMirror) {
        return MoreTypes.asDeclared(typeMirror);
    }

    public static TypeMirror getArrayElementType(TypeMirror mirror) {
        // 给定的TypeMirror提取
        ArrayType arrayType = (ArrayType) mirror;
        return arrayType.getComponentType();
    }

    public static TypeMirror getExtendBound(TypeMirror mirror) {
        return mirror.accept(new SimpleTypeVisitor7<TypeMirror, Void>() {
            @Override
            public TypeMirror visitWildcard(WildcardType t, Void unused) {
                return t.getExtendsBound() == null ? t.getSuperBound() : t.getExtendsBound();
            }
        }, null);
    }

    public static TypeMirror getExtendBoundOrSelf(TypeMirror mirror) {
        return getExtendBound(mirror) == null ? mirror : getExtendBound(mirror);
    }

    public static boolean isCollection(TypeMirror mirror) {
        TypeElement element = ElementUtil.asTypeElement(mirror);
        return ElementUtil.isList(element);
    }

    public static boolean isArray(TypeMirror mirror) {
        return mirror.getKind() == TypeKind.ARRAY;
    }

    public static boolean isError(TypeMirror mirror) {
        return mirror.getKind() == TypeKind.ERROR;
    }

    public static boolean isLong(TypeMirror mirror) {
        return mirror.getKind() == TypeKind.LONG;
    }

    public static boolean equalTypeMirror(TypeMirror m1, TypeMirror m2) {
        if (isPrimitive(m1) && isPrimitive(m2)) {
            return m1.getKind() == m2.getKind();
        }
        if (m1.getKind() == m2.getKind()) {
            // 当前只考虑类的情况
            return ElementUtil.equalTypeElement(ElementUtil.asTypeElement(m1), ElementUtil.asTypeElement(m2));
        }
        return false;
    }

    public static boolean isPrimitive(TypeMirror mirror) {
        return mirror.getKind() == TypeKind.INT ||
                mirror.getKind() == TypeKind.SHORT ||
                mirror.getKind() == TypeKind.LONG ||
                mirror.getKind() == TypeKind.BYTE ||
                mirror.getKind() == TypeKind.CHAR ||
                mirror.getKind() == TypeKind.DOUBLE ||
                mirror.getKind() == TypeKind.FLOAT ||
                mirror.getKind() == TypeKind.BOOLEAN;
    }

    public static boolean isVoid(TypeMirror typeMirror) {
        return typeMirror.getKind() == TypeKind.VOID;
    }

    public static boolean isInt(TypeMirror typeMirror) {
        return typeMirror.getKind() == TypeKind.INT;
    }

    private TypeUtil() {
    }
}
