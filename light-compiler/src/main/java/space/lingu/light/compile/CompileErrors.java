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

package space.lingu.light.compile;

import space.lingu.light.SQLDataType;
import space.lingu.light.compile.javac.CompileType;
import space.lingu.light.compile.struct.DataConverter;

import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author RollW
 */
public final class CompileErrors {
    public static final String BUG_REPORT =
            "If you see this message, it may be a bug. Please report it to us.";


    public static String bugReportWithMessage(String message) {
        return BUG_REPORT + "\nError Message: " + message;
    }

    public static String buildSuccess() {
        return "Light module build success.";
    }

    public static String buildFailed() {
        return "Light module build failed.";
    }

    public static final String DATABASE_NOT_CLASS =
            "@Database must be annotated on a class.";
    public static final String DATABASE_NOT_ABSTRACT_CLASS =
            "Database class must be an abstract class.";
    public static final String DATABASE_NOT_EXTENDS_BASE =
            "Database class must extends LightDatabase class.";

    public static final String DATABASE_ABSTRACT_METHOD_RETURN_TYPE =
            "An abstract method in a database class whose return type " +
                    "must be an abstract class or interface annotated with @Dao";

    public static final String DATABASE_NAME_EMPTY =
            "Database name cannot be empty.";

    public static final String DAO_METHOD_NOT_PARAMLESS =
            "A Dao getter method must be a parameterless method.";

    public static final String DATA_TABLE_NOT_ANNOTATED =
            "A data table class must be annotated with @DataTable.";

    public static final String DATA_TABLE_NOT_CLASS =
            "Please check if the classes in [tables()] contains non-class types.";

    public static final String TABLE_NO_FIELDS =
            "You are trying to create a table with no columns, it should contain at least 1 field.";

    private static final String DUPLICATED_TABLE_NAME = "Duplicate table name '%s' found.";

    public static String duplicatedTableName(String tableName) {
        return String.format(DUPLICATED_TABLE_NAME, tableName);
    }

    private static final String CANNOT_FOUND_CONSTRUCTOR =
            "Cannot find a constructor for %s.";

    public static String cannotFoundConstructor(String typeName) {
        return String.format(CANNOT_FOUND_CONSTRUCTOR, typeName);
    }

    private static final String CANNOT_FOUND_SETTER =
            "Cannot find a setter method for field, please check if its name follow rules" +
                    " or is a private method. Candidates: ";

    private static final String CANNOT_FOUND_GETTER =
            "The getter method of the field cannot be found. " +
                    "Please check whether its name conforms to the rules, " +
                    "or it is a private method, or the return type is different from the field. Candidates: ";


    public static String cannotFoundGetter(Collection<String> candidates) {
        return CANNOT_FOUND_GETTER + candidates;
    }

    public static String cannotFoundSetter(Collection<String> candidates) {
        return CANNOT_FOUND_SETTER + candidates;
    }

    private static final String FIELD_CANNOT_FOUND = "Column name %s defined in %s is not exist," +
            " please check for errors.";

    public static String cannotFoundIndexField(String field) {
        return String.format(FIELD_CANNOT_FOUND, field, "index");
    }

    public static String cannotFoundPrimaryKeyField(String field) {
        return String.format(FIELD_CANNOT_FOUND, field, "primary key");
    }

    private static final String TABLE_COLUMN_NAME_DUPLICATED = "Column name '%s' are duplicated.";

    public static String duplicatedTableColumnName(String columnName) {
        return String.format(TABLE_COLUMN_NAME_DUPLICATED, columnName);
    }

    public static String MULTIPLE_CONSTRUCTOR_ANNOTATED =
            "Multiple constructors are annotated with @Constructor.";

    public static String CANNOT_MATCH_CONSTRUCTOR =
            "Cannot match constructor parameters with data column fields.";

    public static final String MULTIPLE_PRIMARY_KEY_FOUND =
            "More than one defined primary key was found.";

    private static final String TYPE_NOT_ITERATOR = "iterator() not found in Iterable %s.";

    public static String typeNotIterator(TypeElement element) {
        return String.format(TYPE_NOT_ITERATOR, element.getQualifiedName());
    }

    public static final String UNKNOWN_IN_TYPE =
            "Unknown column type: '%s' cannot be processed to read type, specify a @DataConverter method to convert it. " +
                    "Parsed data type: %s.";

    public static final String UNKNOWN_OUT_TYPE =
            "Unknown column type: '%s', cannot be processed to out type, specify a @DataConverter method to convert it. " +
                    "Parsed data type: %s.";

    private static final String TYPE_MISS_MATCH =
            "In/out types miss match, expected: %s, actual: %s.";

    public static String unknownInType(CompileType compileType, SQLDataType type) {
        String name = compileType.getTypeMirror().toString();
        if (type == null) {
            return String.format(UNKNOWN_OUT_TYPE, name, "null");
        }
        return String.format(UNKNOWN_IN_TYPE, name, type.name());
    }

    public static String unknownOutType(CompileType compileType, SQLDataType type) {
        String name = compileType.getTypeMirror().toString();
        if (type == null) {
            return String.format(UNKNOWN_OUT_TYPE, name, "null");
        }
        return String.format(UNKNOWN_OUT_TYPE, name, type.name());
    }

    public static String typeMismatch(SQLDataType finalType, SQLDataType dataType) {
        String finalTypeName = finalType == null ? "null" : finalType.name();
        String dataTypeName = dataType == null ? "null" : dataType.name();
        return String.format(TYPE_MISS_MATCH, finalTypeName, dataTypeName);
    }

    public static final String REPEATED_DATA_CONVERTER =
            "Repeatedly define multiple methods of the same DataConverter type. Conflicts with these:\n %s";

    public static String repeatedDataConverters(List<DataConverter> conflicts) {
        StringJoiner joiner = new StringJoiner(",\n");
        for (DataConverter conflict : conflicts) {
            joiner.add(conflict.toString());
        }
        return String.format(REPEATED_DATA_CONVERTER, joiner);
    }

    public static final String NOT_BOUND_GENERIC_TYPES = "Cannot use unbound generics in DAO " +
            "methods. It needs be bound to a type.";

    public static final String DAO_TOO_MUCH_CONSTRUCTORS =
            "Only can have one constructor that is parameterless or have a Database parameter.";
    public static final String DAO_CONSTRUCTOR_TOO_MUCH_PARAMS =
            "One constructor in DAO can only have one Database parameter or is parameterless.";
    public static final String DAO_CONSTRUCTOR_PARAM_TYPE =
            "Parameter must be of type LightDatabase.";


    public static final String DAO_INVALID_ABSTRACT_METHOD =
            "An abstract method in a dao class must be annotated with one of the annotations below: \n" +
                    "@Insert, @Delete, @Update, @Query";

    public static final String DAO_INVALID_METHOD_PARAMETER =
            "Invalid Method parameter, must be a class or interface.";

    public static final String ACTUAL_PARAM_ANNOTATED_DATATABLE =
            "The actual parameter type should be annotated with @DataTable.";

    public static final String DUPLICATED_METHOD_ANNOTATION =
            "The method only can have one of annotations below : @Insert, @Update, @Query, @Delete.";


    public static final String INSERT_RETURN_TYPE =
            "An insertion method return type must be one of list:" +
                    " void, long, long[], Long, Long[], List<Long>, Collection<Long>, Iterable<Long>.";
    public static final String INSERT_RETURN_TYPE_NOT_MATCHED =
            "Not matched return type with parameter.";

    public static final String DELETE_INVALID_RETURN =
            "Delete method return type invalid, " +
                    "please check the return type and parameter.";

    public static final String UPDATE_INVALID_RETURN =
            "Delete method return type invalid, " +
                    "please check the return type and parameter.";


    public static final String QUERY_SQL_EMPTY = "Query method value cannot be empty, must be a sql expression.";

    public static final String QUERY_UNKNOWN_PARAM =
            "Unknown parameter type.";

    public static final String QUERY_UNKNOWN_RETURN_TYPE =
            "Unable to resolve return type. " +
                    "If you want to return an entity consisting of some columns, " +
                    "follow the instructions.";

    public static final String PARAM_NON_COMPLIANCE =
            "Query/Insert method parameters cannot start with underscore (_).";

    public static final String SQL_CANNOT_BE_EMPTY =
            "The SQL expression in the annotation cannot be empty " +
                    "and must be a meaningful SQL expression.";

    public static final String TRANSACTION_METHOD_NOT_DEFAULT =
            "The transaction method in an interface must have a default implementation.";

    public static final String TRANSACTION_METHOD_ABSTRACT =
            "The transaction method cannot be abstract.";

    public static final String DATA_CONVERTER_TOO_MUCH_PARAMS =
            "A DataConverter method can only have one parameter.";

    public static final String ILLEGAL_DATA_CONVERTERS_CLASS =
            "Illegal class in DataConverters. Please check classes value in DataConverters annotation.";

    public static final String DATA_CONVERTER_NO_PARAM =
            "A DataConverter method must have one parameter.";

    public static final String DATA_CONVERTER_INVALID_RETURN_TYPE =
            "DataConverter method has invalid return type (e.g. void).";

    public static final String DATA_CONVERTER_METHOD_NOT_PUBLIC =
            "A DataConverter method must be public.";

    public static final String DATA_CONVERTER_METHOD_NOT_STATIC =
            "A DataConverter method must be static.";

    private CompileErrors() {
    }


}
