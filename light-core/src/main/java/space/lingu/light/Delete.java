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

package space.lingu.light;

import java.lang.annotation.*;
import java.sql.PreparedStatement;

/**
 * Marks a method in a {@link Dao} annotated class as a delete method.
 * <p>
 * The implementation of the method will delete its parameters from the database.
 * <p>
 * See {@link #value()} for instruction of custom SQL statement.
 *
 * @author RollW
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Delete {
    /**
     * Custom SQL delete statement.
     * <p>
     * If you are writing a method to directly delete columns
     * represented by an entity class,
     * you need set this to {@link #AUTO_GENERATION}.
     * <p>
     * Automatically generated delete statements can only be used
     * for classes where all method parameters are resolvable.
     * <p>
     * But when you want to delete columns by custom conditions,
     * at this point you need to write the SQL statement yourself.
     * <p>
     * e.g. Let us assume that there is a user table,
     * and you are writing a method use as delete users by their id:
     * <pre>
     * {@code @Delete("DELETE FROM user WHERE user_id = {id}")
     * public void deleteByIds(long id);
     * }
     * </pre>
     * You can do this by binding parameters to custom statements.
     * <p>
     * Note: The implementation of the method will call {@link PreparedStatement#executeUpdate()}
     * method to run your SQL statement.
     * You need to ensure that your SQL statement can run normally.
     *
     * @return Custom SQL delete statement
     */
    String value() default AUTO_GENERATION;

    /**
     * Set this value to {@link #value()} to have Light generate the statement.
     */
    String AUTO_GENERATION = "[Auto-Generation]";
}
