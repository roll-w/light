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
import java.util.regex.Pattern;

/**
 * @author RollW
 */
public class SQLExpressionParser {
    private List<String> expressions;
    private final List<Detail> details;

    public SQLExpressionParser(String sql) {
        details = Collections.unmodifiableList(parse(sql));
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

    public List<Detail> getDetails() {
        return details;
    }

    private List<Detail> parse(String sql) {
        List<Detail> details = new ArrayList<>();
        CharacterIterator iterator = new StringCharacterIterator(sql);
        char next;
        StringBuilder builder = new StringBuilder();
        boolean start = false, end = false;
        int startPos = -1, endPos = -1;
        int idx = 0;
        while ((next = iterator.next()) != CharacterIterator.DONE) {
            idx++;
            if (next == '{') {
                start = true;
                startPos = idx;
            }
            if (next == '}') {
                end = true;
                endPos = idx + 1;
            }
            if (start && !end) {
                builder.append(next);
            }
            if (end) {
                String expression = builder.toString()
                        .replaceFirst(Pattern.quote("{"), "");
                Detail detail = new Detail(expression, startPos, endPos);
                details.add(detail);
                start = end = false;
                startPos = endPos = -1;
                builder = new StringBuilder();
            }
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
