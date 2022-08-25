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

import space.lingu.light.SQLDataType;
import space.lingu.light.compile.coder.custom.binder.*;
import space.lingu.light.compile.coder.custom.result.ArrayQueryResultConverter;
import space.lingu.light.compile.coder.custom.result.ListQueryResultConverter;
import space.lingu.light.compile.coder.custom.result.QueryResultConverter;
import space.lingu.light.compile.coder.custom.result.SingleEntityQueryResultConverter;
import space.lingu.light.compile.coder.custom.row.PojoRowConverter;
import space.lingu.light.compile.coder.custom.row.RowConverter;
import space.lingu.light.compile.coder.custom.row.SingleColumnRowConverter;
import space.lingu.light.compile.coder.type.*;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeUtil;
import space.lingu.light.compile.processor.PojoProcessor;
import space.lingu.light.compile.struct.Pojo;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author RollW
 */
public class TypeBinders {
    private final ProcessEnv mEnv;
    private final List<ColumnTypeBinder> mColumnTypeBinders = new ArrayList<>();
    private final List<QueryResultConverter> mQueryResultConverters = new ArrayList<>();
    private VoidColumnTypeBinder mVoidColumnTypeBinder;

    public TypeBinders(ProcessEnv env) {
        mEnv = env;
        initColumnTypeBinders();
    }

    private void initColumnTypeBinders() {
        mColumnTypeBinders.add(new StringColumnTypeBinder(mEnv.getElementUtils().getTypeElement("java.lang.String").asType()));
        List<PrimitiveColumnTypeBinder> primitiveColumnTypeBinders = PrimitiveColumnTypeBinder.create(mEnv);
        mColumnTypeBinders.addAll(primitiveColumnTypeBinders);
        mColumnTypeBinders.addAll(BoxedPrimitiveColumnTypeBinder.create(primitiveColumnTypeBinders, mEnv));
        mVoidColumnTypeBinder = new VoidColumnTypeBinder();
    }

    private void addBinder(ColumnTypeBinder binder) {
        mColumnTypeBinders.add(binder);
    }

    public StatementBinder findStatementBinder(TypeMirror type, SQLDataType dataType) {
        // TODO
        return findColumnTypeBinder(type, dataType);
    }

    public ColumnValueReader findColumnReader(TypeMirror type, SQLDataType dataType) {
        // TODO
        return findColumnTypeBinder(type, dataType);
    }

    public QueryParameterBinder findQueryParameterBinder(TypeMirror typeMirror) {
        if (typeMirror == null) {
            throw new IllegalArgumentException("TypeMirror cannot be null");
        }

        if (TypeUtil.isCollection(typeMirror)) {
            TypeMirror typeArg = TypeUtil.getExtendBoundOrSelf(ElementUtil.getGenericTypes(typeMirror).get(0));
            StatementBinder binder = findStatementBinder(typeArg, null);
            if (binder != null) {
                return new CollectionQueryParameterBinder(binder);
            }
        } else if (TypeUtil.isArray(typeMirror) &&
                TypeUtil.getArrayElementType(typeMirror).getKind() != TypeKind.BYTE) {
            TypeMirror componentType = TypeUtil.getArrayElementType(typeMirror);
            StatementBinder binder = findStatementBinder(componentType, null);
            if (binder != null) {
                return new ArrayQueryParameterBinder(binder);
            }
        } else {
            StatementBinder binder = findStatementBinder(typeMirror, null);
            if (binder != null) {
                return new BasicQueryParameterBinder(binder);
            }
        }

        return null;
    }

    public QueryResultBinder findQueryResultBinder(TypeMirror typeMirror) {
        // TODO
        return new InstantQueryResultBinder(findQueryResultConverter(typeMirror));
    }

    public QueryResultConverter findQueryResultConverter(TypeMirror typeMirror) {
        if (typeMirror == null) {
            throw new IllegalArgumentException("TypeMirror cannot be null");
        }

        if (TypeUtil.isArray(typeMirror) && TypeUtil.getArrayElementType(typeMirror).getKind() != TypeKind.BYTE) {
            RowConverter converter = findRowConverter(TypeUtil.getArrayElementType(typeMirror));
            if (converter != null) {
                return new ArrayQueryResultConverter(converter);
            }
        } else if (ElementUtil.isList(mEnv.getTypeUtils().asElement(typeMirror))) {
            TypeMirror typeArg = TypeUtil.getExtendBoundOrSelf(ElementUtil.getGenericTypes(typeMirror).get(0));
            RowConverter converter = findRowConverter(typeArg);
            if (converter != null) {
                return new ListQueryResultConverter(typeArg, converter);
            }
        }
        RowConverter converter = findRowConverter(typeMirror);
        return new SingleEntityQueryResultConverter(converter);
    }

    public RowConverter findRowConverter(TypeMirror type) {
        if (TypeUtil.isError(type)) {
            return null;
        }
        ColumnValueReader reader = findColumnReader(type, null);
        if (reader != null) {
            return new SingleColumnRowConverter(reader);
        }
        if (mEnv.getTypeUtils().asElement(type) != null && !TypeUtil.isPrimitive(type)) {
            TypeElement element = ElementUtil.asTypeElement(type);
            Pojo pojo = new PojoProcessor(element, mEnv).process();
            // TODO other check
            return new PojoRowConverter(pojo, type);
        }
        return null;
    }


    public ColumnTypeBinder findColumnTypeBinder(TypeMirror type, SQLDataType dataType) {
        if (type.getKind() == TypeKind.ERROR) {
            return null;
        }
        if (type.getKind() == TypeKind.VOID) {
            return mVoidColumnTypeBinder;
        }
        if (!TypeUtil.isPrimitive(type) &&
                ElementUtil.asTypeElement(type).getKind() == ElementKind.ENUM) {
            // basically it will not be null
            return new EnumColumnTypeBinder(type);
        }

        for (ColumnTypeBinder binder : getAllColumnBinders(type)) {
            if (dataType == null || binder.dataType == dataType) {
                return binder;
            }
        }

        return null;
    }


    private List<ColumnTypeBinder> getAllColumnBinders(TypeMirror element) {
        return mColumnTypeBinders.stream().filter(binder ->
                        TypeUtil.equalTypeMirror(binder.type, element))
                .collect(Collectors.toList());
    }
}
