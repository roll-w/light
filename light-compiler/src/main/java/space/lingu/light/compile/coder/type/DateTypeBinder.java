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

import space.lingu.light.LightRuntimeException;
import space.lingu.light.SQLDataType;
import space.lingu.light.compile.JavaPoetClass;
import space.lingu.light.compile.coder.ColumnTypeBinder;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeCompileType;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author RollW
 */
public class DateTypeBinder extends ColumnTypeBinder {
    // supports java.sql.Date, java.sql.Time, java.sql.Timestamp
    // supports java.time.LocalDate, java.time.LocalTime, java.time.LocalDateTime
    // supports long (millis), Long (millis)
    // supports java.util.Date
    // supports java.time.Instant

    // TODO: supports:
    // java.time.ZonedDateTime
    // java.time.OffsetDateTime
    // java.time.OffsetTime

    private final Type type;

    public DateTypeBinder(TypeCompileType typeCompileType,
                          Type type) {
        super(typeCompileType, type.getConvert().dataType);
        this.type = type;
    }

    @Override
    public void readFromResultSet(String outVarName,
                                  String resultSetName,
                                  String indexName,
                                  GenerateCodeBlock block) {
        if (type.equalsType()) {
            readValueWithCheckIndex(
                    outVarName, resultSetName, indexName,
                    type.convert.readMethodName, "null",
                    block
            );
            return;
        }
        boolean needCheckIndex = IndexHelper.isNeedCheckIndex(indexName);
        String readVar = block.getTempVar("_readDateOrTime");
        block.builder()
                .addStatement("final $T $L", type.convert.clazzType, readVar);
        if (needCheckIndex) {
            block.builder().beginControlFlow("if ($L < 0)", indexName)
                    .addStatement("$L = $L", readVar, null)
                    .nextControlFlow("else");
        }

        block.builder().addStatement("$L = $L.$L($L)",
                readVar, resultSetName,
                type.convert.readMethodName, indexName);
        if (needCheckIndex) {
            block.builder().endControlFlow();
        }
        block.builder().addStatement("$L = $T.$L($L)",
                outVarName, JavaPoetClass.UtilNames.DATE_TIME_UTIL,
                type.toMethodName, readVar);

    }

    @Override
    public void bindToStatement(String stmtVarName, String indexVarName,
                                String valueVarName, GenerateCodeBlock block) {
        if (type.equalsType()) {
            bindToStatementWithNullable(
                    stmtVarName, indexVarName,
                    valueVarName, type.convert.bindMethodName,
                    block
            );
            return;
        }

        String bindVar = block.getTempVar("_bindDateOrTime");
        block.builder()
                .addStatement("$T $L = $T.$L($L)", type.convert.clazzType, bindVar,
                        JavaPoetClass.UtilNames.DATE_TIME_UTIL, type.fromMethodName,
                        valueVarName)
                .beginControlFlow("try")
                .beginControlFlow("if ($L == null)", bindVar)
                .addStatement("$L.setNull($L, $L)", stmtVarName, indexVarName, Types.NULL)
                .nextControlFlow("else")
                .addStatement("$L.$L($L, $L)", stmtVarName,
                        type.convert.bindMethodName, indexVarName, bindVar)
                .endControlFlow()
                .nextControlFlow("catch ($T e)", SQLException.class)
                .addStatement("throw new $T(e)", LightRuntimeException.class)
                .endControlFlow();
    }


    public static List<DateTypeBinder> create(ProcessEnv env) {
        List<DateTypeBinder> typeBinders = new ArrayList<>();
        for (Type type : Type.values()) {
            TypeMirror typeMirror = from(type.clazzType, env);
            TypeCompileType typeCompileType = env.getTypeCompileType(typeMirror);
            DateTypeBinder binder = new DateTypeBinder(typeCompileType, type);
            typeBinders.add(binder);
        }
        return typeBinders;
    }

    private static TypeMirror from(Class<?> clazz, ProcessEnv env) {
        if (clazz == long.class) {
            return env.getTypeUtils().getPrimitiveType(TypeKind.LONG);
        }
        return env.getElementUtils()
                .getTypeElement(clazz.getCanonicalName())
                .asType();
    }

    public static List<TypeMirror> getTypes(SQLDataType sqlDataType, ProcessEnv env) {
        List<TypeMirror> typeMirrors = new ArrayList<>();
        List<Type> types = Type.findTypes(sqlDataType);
        for (Type type : types) {
            typeMirrors.add(from(type.clazzType, env));
        }
        return typeMirrors;
    }

    /**
     * @see space.lingu.light.util.DateTimeUtils
     */
    public enum Type {
        DATE_SQL(Convert.DATE, "raw", Date.class),
        TIME_SQL(Convert.TIME, "raw", Time.class),
        TIMESTAMP_SQL(Convert.TIMESTAMP, "raw", Timestamp.class),
        LONG(Convert.TIMESTAMP, "convertLong", long.class),
        LONG_OBJ(Convert.TIMESTAMP, "convertLong", "convertLongObject", Long.class),
        DATE_UTIL(Convert.DATE, "convertDate", java.util.Date.class),
        INSTANT(Convert.TIMESTAMP, "convertInstant", Instant.class),
        LOCAL_DATE(Convert.DATE, "convertLocalDate", LocalDate.class),
        LOCAL_TIME(Convert.TIME, "convertLocalTime", LocalTime.class),
        LOCAL_DATE_TIME(Convert.TIMESTAMP, "convertLocalDateTime", LocalDateTime.class),
        ;

        private final Convert convert;
        /**
         * Converts from the type to java.sql.Date, java.sql.Time, java.sql.Timestamp
         */
        private final String fromMethodName;
        /**
         * Converts java.sql.Date, java.sql.Time, java.sql.Timestamp to the type
         */
        private final String toMethodName;
        private final Class<?> clazzType;

        Type(Convert convert, String fromMethodName, String toMethodName, Class<?> clazzType) {
            this.convert = convert;
            this.fromMethodName = fromMethodName;
            this.toMethodName = toMethodName;
            this.clazzType = clazzType;
        }

        Type(Convert convert, String methodName, Class<?> clazzType) {
            this(convert, methodName, methodName, clazzType);
        }

        public Convert getConvert() {
            return convert;
        }

        public String getFromMethodName() {
            return fromMethodName;
        }

        public String getToMethodName() {
            return toMethodName;
        }

        public Class<?> getClazzType() {
            return clazzType;
        }

        public boolean equalsType() {
            return this.clazzType == convert.clazzType;
        }

        public static List<Type> findTypes(SQLDataType type) {
            List<Type> types = new ArrayList<>();
            for (Type t : values()) {
                if (t.convert.dataType == type) {
                    types.add(t);
                }
            }
            return types;
        }
    }

    public enum Convert {
        DATE(SQLDataType.DATE, Date.class,
                "getDate", "setDate"),
        TIME(SQLDataType.TIME, Time.class,
                "getTime", "setTime"),
        TIMESTAMP(SQLDataType.TIMESTAMP, Timestamp.class,
                "getTimestamp", "setTimestamp"),
        ;
        private final SQLDataType dataType;
        private final Class<?> clazzType;
        private final String readMethodName;
        private final String bindMethodName;

        Convert(SQLDataType dataType, Class<?> clazzType,
                String readMethodName, String bindMethodName) {
            this.dataType = dataType;
            this.clazzType = clazzType;
            this.readMethodName = readMethodName;
            this.bindMethodName = bindMethodName;
        }

        public SQLDataType getDataType() {
            return dataType;
        }

        public Class<?> getClazzType() {
            return clazzType;
        }

        public String getReadMethodName() {
            return readMethodName;
        }

        public String getBindMethodName() {
            return bindMethodName;
        }
    }
}
