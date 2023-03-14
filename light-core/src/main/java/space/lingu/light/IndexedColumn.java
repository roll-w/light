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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate the returned columns of the
 * method in {@link Query}.
 * <p>
 * Will be warned if using in a non {@link Query} method.
 *
 * @author RollW
 * @apiNote This annotation is experimental and may be changed in the future.
 * Perhaps it will be replaced with other optimization methods.
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(IndexedColumns.class)
@LightExperimentalApi
public @interface IndexedColumn {
    /**
     * The name of the column.
     *
     * @return column name
     */
    String value();

    /**
     * The index in the ResultSet.
     *
     * @return index
     */
    int index();
}
