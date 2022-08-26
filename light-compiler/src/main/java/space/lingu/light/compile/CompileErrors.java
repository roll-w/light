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

/**
 * @author RollW
 */
public final class CompileErrors {
    public static final String BUG_REPORT =
            "If you see this message, means you have found a bug. Please report it to us.";

    public static String bugReportWithMessage(String message) {
        return BUG_REPORT + "\nError Message: " + message;
    }

    public static String buildSuccess() {
        return "Light: Light module build success.";
    }

    public static String buildFailed() {
        return "Light: Light module build failed.";
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
                    " void, long, long[], Long, Long[], List<Long>.";
    public static final String INSERT_RETURN_TYPE_NOT_MATCHED =
            "Not matched return type with parameter.";

    public static final String DELETE_INVALID_RETURN =
            "Delete method return type invalid, " +
                    "please check the return type and parameter.";

    public static final String UPDATE_INVALID_RETURN =
            "Delete method return type invalid, " +
                    "please check the return type and parameter.";


    public static final String QUERY_UNKNOWN_PARAM =
            "Unknown parameter type.";

    public static final String PARAM_NON_COMPLIANCE =
            "Query/Insert method parameters cannot start with underscore (_).";

    public static final String SQL_CANNOT_BE_EMPTY =
            "The SQL expression in the annotation cannot be empty " +
                    "and must be a meaningful SQL expression.";


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
}
