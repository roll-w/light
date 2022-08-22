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


import com.squareup.javapoet.TypeName;
import space.lingu.light.LightRuntimeException;
import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.coder.ColumnTypeBinder;
import space.lingu.light.compile.coder.ColumnValueReader;
import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.StatementBinder;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.SQLDataType;
import space.lingu.light.ParseDataType;
import space.lingu.light.util.StringUtil;
import space.lingu.light.util.Triple;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.sql.SQLException;
import java.util.*;

/**
 * 基本数据类型处理
 * @author RollW
 */
public class PrimitiveColumnTypeBinder extends ColumnTypeBinder implements StatementBinder, ColumnValueReader {
    protected final String mGetter;
    protected final String mSetter;

    public PrimitiveColumnTypeBinder(TypeMirror type, String stmtSetter,
                                     String resSetGetter, SQLDataType dataType) {
        super(type, dataType);
        mGetter = resSetGetter;
        mSetter = stmtSetter;
    }

    private String cast() {
        if (mGetter.equals("get" + StringUtil.firstLowerCase(typeName.toString()))) {
            return "";
        }

        return "(" + typeName + ") ";
    }

    @Override
    public void readFromResultSet(String outVarName, String resultSetName, String indexName, GenerateCodeBlock block) {
        block.builder()
                .addStatement("$L = $L$L.$L($L)", outVarName, cast(), resultSetName,
                        mGetter, indexName);
    }

    @Override
    public void bindToStatement(String stmtVarName, String indexVarName, String valueVarName, GenerateCodeBlock block) {
        block.builder()
                .beginControlFlow("try")
                .addStatement("$L.$L($L, $L)", stmtVarName, mSetter, indexVarName, valueVarName)
                .nextControlFlow("catch ($T e)", SQLException.class)
                .addStatement("throw new $T(e)", LightRuntimeException.class)
                .endControlFlow();
    }

    public static List<PrimitiveColumnTypeBinder> create(ProcessEnv env) {
        List<PrimitiveColumnTypeBinder> binderList = new ArrayList<>();
        sCandicateTypeMapping.forEach((parseDataType, triple) -> {
            SQLDataType sqlDataType;
            switch (parseDataType) {
                case SHORT:
                case INT:
                case BYTE:
                    sqlDataType = SQLDataType.INT; break;
                case LONG:
                    sqlDataType = SQLDataType.LONG; break;
                case DOUBLE:
                    sqlDataType = SQLDataType.DOUBLE; break;
                case FLOAT:
                    sqlDataType = SQLDataType.FLOAT; break;
                case CHAR:
                    sqlDataType = SQLDataType.CHAR; break;
                case BOOLEAN:
                    sqlDataType = SQLDataType.BOOLEAN; break;
                default: {
                    throw new LightCompileException("Illegal type.");
                }
            }

            binderList.add(new PrimitiveColumnTypeBinder(env.getTypeUtils().getPrimitiveType(TypeKind.valueOf(triple.first.toString().toUpperCase(Locale.US))),
                    triple.second, triple.third, sqlDataType));
        });
        return binderList;
    }

    private static final Map<ParseDataType, Triple<TypeName, String, String>> sCandicateTypeMapping = new EnumMap<>(ParseDataType.class);
    static {
        sCandicateTypeMapping.put(ParseDataType.INT, Triple.createTriple(TypeName.INT, "setInt", "getInt"));
        sCandicateTypeMapping.put(ParseDataType.SHORT, Triple.createTriple(TypeName.SHORT, "setShort", "getShort"));
        sCandicateTypeMapping.put(ParseDataType.LONG, Triple.createTriple(TypeName.LONG, "setLong", "getLong"));
        sCandicateTypeMapping.put(ParseDataType.CHAR, Triple.createTriple(TypeName.CHAR, "setChar", "getChar"));
        sCandicateTypeMapping.put(ParseDataType.BYTE, Triple.createTriple(TypeName.BYTE, "setByte", "getByte"));
        sCandicateTypeMapping.put(ParseDataType.DOUBLE, Triple.createTriple(TypeName.DOUBLE, "setDouble", "getDouble"));
        sCandicateTypeMapping.put(ParseDataType.FLOAT, Triple.createTriple(TypeName.FLOAT, "setFloat", "getFloat"));
        sCandicateTypeMapping.put(ParseDataType.BOOLEAN, Triple.createTriple(TypeName.BOOLEAN, "setBoolean", "getBoolean"));
    }

}
