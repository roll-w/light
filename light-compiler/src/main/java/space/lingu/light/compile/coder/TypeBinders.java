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

import com.google.auto.common.MoreTypes;
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
import space.lingu.light.compile.struct.DataConverter;
import space.lingu.light.compile.struct.Pojo;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author RollW
 */
public class TypeBinders {
    private final ProcessEnv mEnv;
    private final List<ColumnTypeBinder> mColumnTypeBinders = new ArrayList<>();
    private final List<QueryResultConverter> mQueryResultConverters = new ArrayList<>();
    private VoidColumnTypeBinder mVoidColumnTypeBinder;

    private final List<TypeConverter> mTypeConverters = new LinkedList<>();

    public TypeBinders(ProcessEnv env) {
        mEnv = env;
        initColumnTypeBinders();
        loadAllKnownTypes();
    }

    private void initColumnTypeBinders() {
        mColumnTypeBinders.add(StringColumnTypeBinder.create(mEnv));
        mColumnTypeBinders.add(ByteArrayColumnTypeBinder.create(mEnv));
        List<PrimitiveColumnTypeBinder> primitiveColumnTypeBinders =
                PrimitiveColumnTypeBinder.create(mEnv);
        mColumnTypeBinders.addAll(primitiveColumnTypeBinders);
        mColumnTypeBinders.addAll(BoxedPrimitiveColumnTypeBinder.create(primitiveColumnTypeBinders, mEnv));
        mVoidColumnTypeBinder = new VoidColumnTypeBinder();
    }

    private void addBinder(ColumnTypeBinder binder) {
        mColumnTypeBinders.add(binder);
    }

    public StatementBinder findStatementBinder(TypeMirror type, SQLDataType dataType) {
        if (TypeUtil.isError(type)) {
            return null;
        }
        ColumnTypeBinder binder = findColumnTypeBinder(type, dataType);
        if (binder != null) {
            return binder;
        }
        TypeConverter converter = findConverterInto(type, getTypes(dataType));
        if (converter == null) {
            return null;
        }
        List<ColumnTypeBinder> binders = getAllColumnBinders(converter.to);
        ColumnTypeBinder converterBinder = binders
                .stream()
                .findFirst()
                .orElse(null);
        if (converterBinder == null) {
            return null;
        }

        return new CompositeTypeBinder(type, converterBinder,
                converter, null);
    }

    public ColumnValueReader findColumnReader(TypeMirror type, SQLDataType dataType) {
        if (TypeUtil.isError(type)) {
            return null;
        }
        ColumnTypeBinder binder = findColumnTypeBinder(type, dataType);
        if (binder != null) {
            return binder;
        }
        TypeConverter converter = findConverterRead(getTypes(dataType), type);
        if (converter == null) {
            return null;
        }
        ColumnTypeBinder converterBinder = getAllColumnBinders(converter.from)
                .stream()
                .findFirst()
                .orElse(null);
        if (converterBinder == null) {
            return null;
        }

        return new CompositeTypeBinder(type, converterBinder,
                null, converter);
    }

    private TypeConverter findConverterInto(TypeMirror in, List<TypeMirror> out) {
        return findConverter(
                Collections.singletonList(in),
                (out == null || out.isEmpty()) ? mHandleableTypes : out
        );
    }

    private TypeConverter findConverterRead(List<TypeMirror> in, TypeMirror out) {
        return findConverter(
                (in == null || in.isEmpty()) ? mHandleableTypes : in,
                Collections.singletonList(out)
        );
    }

    private TypeConverter findConverter(TypeMirror in, TypeMirror out) {
        return findConverter(
                Collections.singletonList(in),
                Collections.singletonList(out)
        );
    }

    private TypeConverter findConverter(List<TypeMirror> ins, List<TypeMirror> outs) {
        if (ins.isEmpty()) {
            return null;
        }
        for (TypeMirror in : ins) {
            if (outs.stream().anyMatch(out ->
                    mEnv.getTypeUtils().isSameType(in, out))) {
                return new NoOpTypeConverter(in);
            }
        }

        List<TypeMirror> excludes = new ArrayList<>();
        Deque<TypeConverter> queue = new LinkedList<>();

        for (TypeMirror in : ins) {
            List<TypeConverter> candidates = getAllTypeConverters(in, excludes);
            TypeConverter converter = findMatching(candidates, outs);
            if (converter != null) {
                return converter;
            }
            candidates.forEach(typeConverter -> {
                excludes.add(typeConverter.to);
                queue.add(typeConverter);
            });
        }
        excludes.addAll(ins);
        while (!queue.isEmpty()) {
            TypeConverter top = queue.pop();
            TypeMirror from = top.from;
            List<TypeConverter> candidates = getAllTypeConverters(from, excludes);
            TypeConverter converter = findMatching(candidates, outs);
            if (converter != null) {
                return new CombinedTypeConverter(top, converter);
            }
            candidates.forEach(typeConverter -> {
                excludes.add(typeConverter.to);
                queue.add(new CombinedTypeConverter(top, typeConverter));
            });
        }
        return null;
    }

    private TypeConverter findMatching(List<TypeConverter> converters, List<TypeMirror> outs) {
        TypeConverter fallback = null;
        for (TypeConverter converter : converters) {
            for (TypeMirror out : outs) {
                if (mEnv.getTypeUtils().isSameType(out, converter.to)) {
                    return converter;
                } else if (fallback == null &&
                        mEnv.getTypeUtils().isAssignable(out, converter.to)) {
                    fallback = converter;
                }
            }
        }
        return fallback;
    }


    private List<TypeConverter> getAllTypeConverters(TypeMirror in, List<TypeMirror> excludes) {
        return mTypeConverters.stream().filter(typeConverter ->
                mEnv.getTypeUtils().isAssignable(typeConverter.from, in) &&
                        excludes.stream().noneMatch(con ->
                                mEnv.getTypeUtils().isAssignable(con, in))
        ).collect(Collectors.toList());
    }

    public void registerDataConverters(List<DataConverter> dataConverterList) {
        dataConverterList.forEach(dataConverter -> {
            mTypeConverters.add(new DataConverterTypeConverter(dataConverter));
            mHandleableTypes.add(dataConverter.getToType());
        });
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
        if (converter == null) {
            return null;
        }
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
        TypeElement asElement = ElementUtil.asTypeElement(type);
        if (!TypeUtil.isPrimitive(type) &&
                asElement != null && asElement.getKind() == ElementKind.ENUM) {
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

    private final List<TypeMirror> mHandleableTypes = new LinkedList<>();

    private void loadAllKnownTypes() {
        mHandleableTypes.clear();
        mHandleableTypes.addAll(getTypes(SQLDataType.INT));
        mHandleableTypes.addAll(getTypes(SQLDataType.LONG));
        mHandleableTypes.addAll(getTypes(SQLDataType.BINARY));
        mHandleableTypes.addAll(getTypes(SQLDataType.BOOLEAN));
        mHandleableTypes.addAll(getTypes(SQLDataType.DOUBLE));
        mHandleableTypes.addAll(getTypes(SQLDataType.FLOAT));
        mHandleableTypes.addAll(getTypes(SQLDataType.VARCHAR));
    }

    public List<TypeMirror> getTypes(SQLDataType dataType) {
        List<TypeMirror> typeMirrors = new LinkedList<>();
        if (dataType == null) {
            return mHandleableTypes;
        }
        switch (dataType) {
            case INT:
            case CHAR:
                typeMirrors.addAll(getIncludeBoxed(TypeKind.INT));
                typeMirrors.addAll(getIncludeBoxed(TypeKind.SHORT));
                typeMirrors.addAll(getIncludeBoxed(TypeKind.BYTE));
                break;
            case LONG:
                typeMirrors.addAll(getIncludeBoxed(TypeKind.LONG));
                break;
            case BINARY:
                typeMirrors.add(getArrayType(getType(TypeKind.BYTE)));
                break;
            case BOOLEAN:
                typeMirrors.add(getType(TypeKind.BOOLEAN));
                break;
            case DOUBLE:
                typeMirrors.add(getType(TypeKind.DOUBLE));
                break;
            case FLOAT:
                typeMirrors.add(getType(TypeKind.FLOAT));
                break;
            case LONGTEXT:
            case VARCHAR:
            case TEXT:
                typeMirrors.add(getType(String.class));
                break;
        }
        return typeMirrors;
    }

    private TypeMirror getType(TypeKind kind) {
        return mEnv.getTypeUtils().getPrimitiveType(kind);
    }

    private TypeMirror getType(Class<?> clz) {
        return mEnv.getElementUtils().getTypeElement(clz.getCanonicalName()).asType();
    }

    private List<TypeMirror> getIncludeBoxed(TypeKind kind) {
        TypeMirror mirror = mEnv.getTypeUtils().getPrimitiveType(kind);
        TypeMirror boxed = mEnv.getTypeUtils().boxedClass(
                MoreTypes.asPrimitiveType(mirror)).asType();
        return Arrays.asList(mirror, boxed);
    }

    private TypeMirror getArrayType(TypeMirror mirror) {
        return mEnv.getTypeUtils().getArrayType(mirror);
    }


}
