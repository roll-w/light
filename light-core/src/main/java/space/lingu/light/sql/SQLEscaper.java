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

package space.lingu.light.sql;

import space.lingu.light.util.StringUtils;

/**
 * Escapes parameters in SQL statement.
 *
 * @author RollW
 */
public interface SQLEscaper {
    /**
     * Back quote.
     */
    String BACK_QUOTE = "`";

    /**
     * Escapes parameter in SQL statement.
     * <p>
     * We provide a default method with using
     * back quote ({@code `}) to escape parameter.
     * It works in most databases.
     * <p>
     * For example: with input {@code user}, returns {@code `user`};
     * with input {@code `user`}, returns itself.
     *
     * @param param parameter in SQL statement
     * @return escaped parameter
     * @throws IllegalArgumentException when param is null or empty, we throw an exception.
     */
    default String escapeParam(String param) throws IllegalArgumentException {
        if (StringUtils.isEmpty(param)) {
            throw new IllegalArgumentException("Parameter cannot be empty.");
        }
        if (param.startsWith(BACK_QUOTE) && param.endsWith(BACK_QUOTE)) {
            return param;
        }
        return BACK_QUOTE + param + BACK_QUOTE;
    }
}
