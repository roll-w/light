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
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.coder.custom.binder.ArrayQueryParameterBinder;
import space.lingu.light.compile.coder.custom.binder.BasicQueryParameterBinder;
import space.lingu.light.compile.coder.custom.binder.CollectionQueryParameterBinder;
import space.lingu.light.compile.coder.custom.binder.InstantQueryResultBinder;
import space.lingu.light.compile.coder.custom.binder.QueryParameterBinder;
import space.lingu.light.compile.coder.custom.binder.QueryResultBinder;
import space.lingu.light.compile.coder.custom.result.ArrayQueryResultConverter;
import space.lingu.light.compile.coder.custom.result.ListQueryResultConverter;
import space.lingu.light.compile.coder.custom.result.QueryResultConverter;
import space.lingu.light.compile.coder.custom.result.RawQueryResultConverter;
import space.lingu.light.compile.coder.custom.result.SingleEntityQueryResultConverter;
import space.lingu.light.compile.coder.custom.row.PojoRowConverter;
import space.lingu.light.compile.coder.custom.row.RowConverter;
import space.lingu.light.compile.coder.custom.row.SingleColumnRowConverter;
import space.lingu.light.compile.coder.type.*;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.javac.TypeUtils;
import space.lingu.light.compile.processor.PojoProcessor;
import space.lingu.light.compile.processor.ReturnTypes;
import space.lingu.light.compile.processor.SQLDataTypeUtils;
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
    // TODO: refactor TypeBinders

    private final ProcessEnv mEnv;
    private final List<ColumnTypeBinder> mColumnTypeBinders = new ArrayList<>();
    private final List<QueryResultConverter> mQueryResultConverters = new ArrayList<>();
    private VoidColumnTypeBinder mVoidColumnTypeBinder;

    private final List<TypeConverter> mTypeConverters = new LinkedList<>();

    public TypeBinders(ProcessEnv env) {
        mEnv = env;
        initColumnTypeBinders();
    }

    private void initColumnTypeBinders() {
        mColumnTypeBinders.add(StringColumnTypeBinder.create(mEnv));
        mColumnTypeBinders.add(BigDecimalColumnTypeBinder.create(mEnv));
        mColumnTypeBinders.add(ByteArrayColumnTypeBinder.create(mEnv));
        List<PrimitiveColumnTypeBinder> primitiveColumnTypeBinders =
                PrimitiveColumnTypeBinder.create(mEnv);
        mColumnTypeBinders.addAll(DateTypeBinder.create(mEnv));
        mColumnTypeBinders.addAll(primitiveColumnTypeBinders);
        mColumnTypeBinders.addAll(BoxedPrimitiveColumnTypeBinder.create(primitiveColumnTypeBinders, mEnv));
        mVoidColumnTypeBinder = new VoidColumnTypeBinder();

        loadInKnownTypes(mColumnTypeBinders);
        loadAllKnownTypes();
    }

    private final Map<SQLDataType, List<TypeMirror>> mKnownTypes =
            new HashMap<>();

    private void loadInKnownTypes(List<ColumnTypeBinder> binders) {
        for (ColumnTypeBinder binder : binders) {
            SQLDataType dataType = binder.getDataType();
            if (dataType == null) {
                continue;
            }
            List<TypeMirror> types = mKnownTypes.computeIfAbsent(
                    dataType, k -> new ArrayList<>());
            types.add(binder.type().getTypeMirror());
        }
    }

    public StatementBinder findStatementBinder(TypeCompileType typeCompileType,
                                               SQLDataType dataType) {
        TypeMirror type = typeCompileType.getTypeMirror();
        if (TypeUtils.isError(type)) {
            return null;
        }
        ColumnTypeBinder binder = findColumnTypeBinder(
                typeCompileType, dataType);
        if (binder != null) {
            return binder;
        }
        boolean findsAll = dataType == null || dataType == SQLDataType.UNDEFINED;
        TypeConverter converter = findConverterInto(type, findTypesFor(dataType, findsAll));
        if (converter == null) {
            return findEnumColumnTypeBinder(typeCompileType);
        }
        List<ColumnTypeBinder> binders = getAllColumnBinders(converter.to);
        ColumnTypeBinder converterBinder = binders
                .stream()
                .findFirst()
                .orElse(null);
        if (converterBinder != null) {
            return new CompositeTypeBinder(typeCompileType, converterBinder,
                    converter, null);
        }
        return null;
    }

    public ColumnValueReader findColumnReader(TypeCompileType typeCompileType,
                                              SQLDataType dataType) {
        TypeMirror type = typeCompileType.getTypeMirror();
        if (TypeUtils.isError(type)) {
            return null;
        }
        ColumnTypeBinder binder = findColumnTypeBinder(typeCompileType, dataType);
        if (binder != null) {
            return binder;
        }
        boolean findsAll = dataType == null || dataType == SQLDataType.UNDEFINED;
        TypeConverter converter = findConverterRead(findTypesFor(dataType, findsAll), type);
        if (converter == null) {
            return findEnumColumnTypeBinder(typeCompileType);
        }
        ColumnTypeBinder converterBinder = getAllColumnBinders(converter.from)
                .stream()
                .findFirst()
                .orElse(null);
        if (converterBinder == null) {
            return null;
        }

        return new CompositeTypeBinder(
                typeCompileType, converterBinder,
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
        return mTypeConverters.stream()
                .filter(typeConverter ->
                        mEnv.getTypeUtils().isAssignable(typeConverter.from, in) &&
                                excludes.stream().noneMatch(con -> mEnv.getTypeUtils().isAssignable(con, in))
                ).collect(Collectors.toList());
    }

    public void registerDataConverters(List<DataConverter> dataConverterList) {
        dataConverterList.forEach(dataConverter -> {
            mTypeConverters.add(new DataConverterTypeConverter(dataConverter));
        });
    }

    public QueryParameterBinder findQueryParameterBinder(TypeCompileType typeCompileType) {
        if (typeCompileType == null) {
            throw new IllegalArgumentException("TypeCompileType cannot be null");
        }
        QueryParameterBinder collectionBinder =
                tryFindQueryParamBinderCollection(typeCompileType);
        if (collectionBinder != null) {
            return collectionBinder;
        }
        QueryParameterBinder arrayBinder =
                tryFindQueryParamBinderArray(typeCompileType);
        if (arrayBinder != null) {
            return arrayBinder;
        }
        SQLDataType dataType = SQLDataTypeUtils
                .recognizeSQLDataType(null, typeCompileType);
        StatementBinder binder =
                findStatementBinder(typeCompileType, dataType);

        if (binder != null) {
            return new BasicQueryParameterBinder(binder);
        }
        return null;
    }

    private QueryParameterBinder tryFindQueryParamBinderCollection(
            TypeCompileType typeCompileType) {
        if (!TypeUtils.isCollection(mEnv, typeCompileType.getTypeMirror())) {
            return null;
        }
        List<? extends TypeMirror> genericTypes =
                TypeUtils.getGenericTypes(typeCompileType.getTypeMirror());
        TypeMirror typeArg = TypeUtils.getExtendBoundOrSelf(
                genericTypes.get(0));
        TypeCompileType typeArgType = mEnv.getTypeCompileType(typeArg);

        SQLDataType dataType =
                SQLDataTypeUtils.recognizeSQLDataType(null, typeArgType);
        StatementBinder binder = findStatementBinder(typeArgType, dataType);
        if (binder != null) {
            return new CollectionQueryParameterBinder(binder);
        }
        return null;
    }

    private QueryParameterBinder tryFindQueryParamBinderArray(
            TypeCompileType typeCompileType) {
        if (!TypeUtils.isArray(typeCompileType.getTypeMirror())) {
            return null;
        }
        if (TypeUtils.getArrayElementType(typeCompileType.getTypeMirror()).getKind() == TypeKind.BYTE) {
            return null;
        }

        TypeMirror componentType = TypeUtils
                .getArrayElementType(typeCompileType.getTypeMirror());
        TypeCompileType componentTypeCompileType =
                mEnv.getTypeCompileType(componentType);
        SQLDataType dataType =
                SQLDataTypeUtils.recognizeSQLDataType(null, componentTypeCompileType);
        StatementBinder binder = findStatementBinder(componentTypeCompileType, dataType);

        if (binder != null) {
            return new ArrayQueryParameterBinder(binder);
        }
        return null;
    }


    public QueryResultBinder findQueryResultBinder(TypeCompileType typeCompileType) {
        QueryResultConverter resultConverter = findQueryResultConverter(typeCompileType);
        if (resultConverter == null) {
            return null;
        }
        return new InstantQueryResultBinder(findQueryResultConverter(typeCompileType));
    }

    public QueryResultConverter findQueryResultConverter(
            TypeCompileType typeCompileType) {
        if (typeCompileType == null) {
            throw new IllegalArgumentException("TypeCompileType cannot be null");
        }
        TypeMirror typeMirror = typeCompileType.getTypeMirror();
        if (RawQueryResultConverter.isRaw(typeCompileType, mEnv)) {
            return RawQueryResultConverter.create(mEnv);
        }

        RowConverter arrayConverter = tryFindRowConverterArrayType(typeCompileType);
        if (arrayConverter != null) {
            return new ArrayQueryResultConverter(arrayConverter);
        }

        boolean isIterable = TypeUtils.isIterable(mEnv, typeMirror);
        if (isIterable) {
            RowConverter converter = tryFindRowConverterIterator(typeCompileType);
            if (converter != null) {
                return new ListQueryResultConverter(
                        converter.getOutType(),
                        converter
                );
            }
        }
        SQLDataType sqlDataType = SQLDataTypeUtils.recognizeSQLDataType(
                null,
                typeCompileType
        );
        RowConverter converter = findRowConverter(typeCompileType, sqlDataType);
        if (converter == null) {
            return null;
        }
        return new SingleEntityQueryResultConverter(converter);
    }

    private RowConverter tryFindRowConverterArrayType(TypeCompileType typeCompileType) {
        TypeMirror typeMirror = typeCompileType.getTypeMirror();
        if (!TypeUtils.isArray(typeMirror)) {
            return null;
        }
        TypeMirror arrayType = TypeUtils.getArrayElementType(typeMirror);
        if (arrayType.getKind() == TypeKind.BYTE) {
            return null;
        }
        TypeCompileType arrayElementType =
                mEnv.getTypeCompileType(arrayType);

        SQLDataType preprocess = SQLDataTypeUtils.recognizeSQLDataType(
                null,
                arrayElementType
        );
        return findRowConverter(arrayElementType, preprocess);
    }

    private RowConverter tryFindRowConverterIterator(TypeCompileType typeCompileType) {
        TypeElement element = typeCompileType.getElement();
        TypeMirror typeMirror = typeCompileType.getTypeMirror();

        if (!ReturnTypes.isLegalCollectionReturnType(element)) {
            throw new LightCompileException(CompileErrors.QUERY_UNKNOWN_RETURN_TYPE);
        }
        List<? extends TypeMirror> genericTypes = TypeUtils.getGenericTypes(typeMirror);
        if (genericTypes == null || genericTypes.isEmpty()) {
            throw new LightCompileException(CompileErrors.NOT_BOUND_GENERIC_TYPES);
        }
        TypeMirror typeArg = TypeUtils.getExtendBoundOrSelf(
                genericTypes.get(0)
        );
        TypeCompileType typeArgCompileType = mEnv
                .getTypeCompileType(typeArg);

        SQLDataType preprocess = SQLDataTypeUtils.recognizeSQLDataType(
                null, typeArgCompileType);
        return findRowConverter(typeArgCompileType, preprocess);
    }

    public RowConverter findRowConverter(TypeCompileType typeCompileType,
                                         SQLDataType dataType) {
        TypeMirror typeMirror = typeCompileType.getTypeMirror();
        if (TypeUtils.isError(typeMirror)) {
            return null;
        }
        ColumnValueReader reader = findColumnReader(typeCompileType, dataType);
        if (reader != null) {
            return new SingleColumnRowConverter(reader);
        }
        if (typeCompileType.getElement() != null && !TypeUtils.isPrimitive(typeMirror)) {
            PojoProcessor processor = new PojoProcessor(
                    typeCompileType,
                    mEnv
            );
            Pojo pojo = processor.process();
            // TODO: other check
            return new PojoRowConverter(pojo, typeCompileType);
        }
        return null;
    }


    private ColumnTypeBinder findColumnTypeBinder(TypeCompileType typeCompileType,
                                                  SQLDataType dataType) {
        TypeMirror type = typeCompileType.getTypeMirror();
        if (type.getKind() == TypeKind.ERROR) {
            return null;
        }
        if (type.getKind() == TypeKind.VOID) {
            return mVoidColumnTypeBinder;
        }
        //  no more attempts to enum type here, put it last
        for (ColumnTypeBinder binder : getAllColumnBinders(type)) {
            if (dataType == null || binder.dataType == dataType) {
                return binder;
            }
        }
        return null;
    }

    private ColumnTypeBinder findDefaultTypeBinder(TypeCompileType type) {
        // here provides fallback builtin type binder, if any.
        // now here are just enum type.
        return findEnumColumnTypeBinder(type);
    }

    private EnumColumnTypeBinder findEnumColumnTypeBinder(TypeCompileType type) {
        if (!checkIfEnumType(type)) {
            return null;
        }
        return new EnumColumnTypeBinder(type);
    }

    private boolean checkIfEnumType(TypeCompileType type) {
        if (TypeUtils.isPrimitive(type.getTypeMirror())) {
            return false;
        }
        return type.getElement() != null &&
                type.getElement().getKind() == ElementKind.ENUM;
    }

    private List<ColumnTypeBinder> getAllColumnBinders(TypeMirror element) {
        return mColumnTypeBinders.stream()
                .filter(binder -> TypeUtils.equalTypeMirror(
                        binder.type.getTypeMirror(), element))
                .collect(Collectors.toList());
    }

    private final List<TypeMirror> mHandleableTypes = new LinkedList<>();

    private void loadAllKnownTypes() {
        mHandleableTypes.clear();
        for (SQLDataType value : SQLDataType.values()) {
            mHandleableTypes.addAll(getTypes(value));
        }
    }

    private List<TypeMirror> findTypesFor(SQLDataType dataType, boolean findsAll) {
        if (findsAll || dataType == null || dataType == SQLDataType.UNDEFINED) {
            return mHandleableTypes;
        }
        return getTypes(dataType);
    }

    private List<TypeMirror> getTypes(SQLDataType dataType) {
        if (dataType == null || dataType == SQLDataType.UNDEFINED) {
            return mHandleableTypes;
        }
        List<TypeMirror> typeMirrors = mKnownTypes.get(dataType);
        if (typeMirrors == null) {
            return Collections.emptyList();
        }
        return typeMirrors;
    }
}
