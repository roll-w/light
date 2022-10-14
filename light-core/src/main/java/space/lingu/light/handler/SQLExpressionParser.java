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

package space.lingu.light.handler;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Internal API. Parse a sql expression.
 *
 * @author RollW
 */
public class SQLExpressionParser {
    private static final int INITIAL = -1;

    private static final char START = '{';
    private static final char END = '}';
    private static final char INVALID = CharacterIterator.DONE;

    private List<String> expressions;
    private final String sql;
    private final String unescapedSql;
    private final List<Detail> details;

    public SQLExpressionParser(String sql) {
        this.sql = sql;
        this.unescapedSql = unescapedWithPlaceholder();
        this.details = parseWith();
    }

    private List<Detail> parseWith() {
        return Collections.unmodifiableList(parse(unescapedSql));
    }


    public List<String> getExpressions() {
        if (expressions == null) {
            expressions = new ArrayList<>();
            details.forEach(detail ->
                    expressions.add(detail.expression));
            expressions = Collections.unmodifiableList(expressions);
        }
        return expressions;
    }

    /**
     * A temp placeholder, in order to ensure the same position after escaping.
     */
    static final String TEMP_PLACEHOLDER = "%";

    private String unescapedWithPlaceholder() {
        return sql
                .replaceAll(Pattern.quote("{{"), TEMP_PLACEHOLDER)
                .replaceAll(Pattern.quote("}}"), TEMP_PLACEHOLDER);
    }

    public String unescaped(String s) {
        return unescape(s);
    }

    public static String unescape(String s) {
        return s
                .replaceAll(Pattern.quote("{{"), "{")
                .replaceAll(Pattern.quote("}}"), "}");
    }

    public String toUppercase() {
        String unescaped = this.unescapedSql.toUpperCase(Locale.US);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < details.size(); i++) {
            SQLExpressionParser.Detail detail = details.get(i);
            if (i == 0) {
                builder.append(unescaped, 0, detail.start);
            }
            builder.append("{").append(detail.expression).append("}");
            if (i != 0) {
                builder.append(unescaped, details.get(i - 1).end, detail.start);
            }
            if (i == details.size() - 1) {
                builder.append(unescaped, detail.end, unescaped.length());
            }
        }
        return builder.toString();
    }

    /**
     * Returns parsed details.
     * <p>
     * The start and end positions represent positions
     * within an unescaped expression string.
     *
     * @return list of {@link Detail}
     */
    public List<Detail> getDetails() {
        return details;
    }

    private List<Detail> parse(String sql) {
        List<Detail> details = new ArrayList<>();
        CharacterIterator iterator = new StringCharacterIterator(sql);
        char next = iterator.current();
        StringBuilder builder = new StringBuilder();
        boolean start = false, end = false;
        int startPos = INITIAL, endPos = INITIAL;
        int idx = 0;
        do {
            if (next == START) {
                start = true;
                startPos = idx;
            }
            if (next == END) {
                end = true;
                endPos = idx + 1;
            }

            if (start && !end) {
                builder.append(next);
            }

            if (end && !start) {
                // only one '}' is meaningless
                end = false;
                endPos = INITIAL;
            }
            if (start && end) {
                String expression = builder
                        .substring(1)
                        .substring(0, builder.length() - 1);
                Detail detail = new Detail(expression, startPos, endPos);
                details.add(detail);
                start = end = false;
                startPos = endPos = INITIAL;
                builder = new StringBuilder();
            }

            idx++;

        } while ((next = iterator.next()) != INVALID);

        if (start) {
            throw new IllegalArgumentException("Can't reach the end, can't find the right expression.");
        }
        return details;
    }

    public static class Detail {
        public final String expression;
        public final int start;
        public final int end;

        public Detail(String expression, int start, int end) {
            this.expression = expression;
            this.start = start;
            this.end = end;
        }
    }
}
