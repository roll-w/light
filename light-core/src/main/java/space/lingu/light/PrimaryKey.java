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

/**
 * Marks column(s) as Primary key(s).
 *
 * @author RollW
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface PrimaryKey {
    /**
     * Whether to automatically generate the primary key.
     * <p>
     * If you are define composite primary key, this will not work.
     *
     * @return If automatically generate or not.
     */
    boolean autoGenerate() default false;
}
