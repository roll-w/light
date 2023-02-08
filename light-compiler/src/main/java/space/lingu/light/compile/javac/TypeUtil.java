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

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor7;
import javax.lang.model.util.Types;
import java.util.List;

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
        // extracts from given TypeMirror
        if (mirror instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) mirror;
            return arrayType.getComponentType();
        }
        return mirror;
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

    public static boolean isCollection(ProcessEnv env, TypeMirror typeMirror) {
        TypeMirror erasure = env.getTypeUtils().erasure(typeMirror);
        return TypeUtil.isAssignedFrom(
                env.getTypeUtils(),
                erasure,
                env.getElementUtils().getTypeElement("java.util.Collection").asType()
        );
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

    /**
     * Only used in simple type.
     */
    public static boolean equalTypeMirror(TypeMirror m1, TypeMirror m2) {
        if (m1.getKind() != m2.getKind()) {
            return false;
        }
        if (isPrimitive(m1) && isPrimitive(m2)) {
            return true;
        }

        if (m1.getKind() == TypeKind.ARRAY) {
            ArrayType m1A = (ArrayType) m1;
            ArrayType m2A = (ArrayType) m2;

            return equalTypeMirror(m1A.getComponentType(), m2A.getComponentType());
        }
        try {
            List<? extends TypeMirror> g1 = getGenericTypes(m1);
            List<? extends TypeMirror> g2 = getGenericTypes(m2);
            if (g1.size() != g2.size()) {
                return false;
            }
            int size = g1.size();
            for (int i = 0; i < size; i++) {
                TypeMirror gM1 = g1.get(i);
                TypeMirror gM2 = g2.get(i);
                boolean r = equalTypeMirror(gM1, gM2);
                if (!r) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ElementUtil.equalTypeElement(ElementUtil.asTypeElement(m1), ElementUtil.asTypeElement(m2));
    }

    public static List<? extends TypeMirror> getGenericTypes(TypeMirror mirror) {
        try {
            return MoreTypes.asDeclared(mirror).getTypeArguments();
        } catch (Exception e) {
            return null;
        }
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

    public static boolean isNone(TypeMirror typeMirror) {
        return typeMirror.getKind() == TypeKind.NONE;
    }

    public static boolean isInt(TypeMirror typeMirror) {
        return typeMirror.getKind() == TypeKind.INT;
    }

    public static boolean isIterable(ProcessEnv env, TypeMirror typeMirror) {
        TypeMirror erasure = env.getTypeUtils().erasure(typeMirror);
        return TypeUtil.isAssignedFrom(
                env.getTypeUtils(),
                erasure,
                env.getElementUtils().getTypeElement("java.lang.Iterable").asType()
        );
    }


    private TypeUtil() {
    }


}
