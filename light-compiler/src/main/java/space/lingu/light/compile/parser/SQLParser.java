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

package space.lingu.light.compile.parser;

import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.handler.SQLExpressionParser;
import space.lingu.light.util.StringUtil;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author RollW
 */
public class SQLParser {
    // TODO
    private final String sql;
    private final ExecutableElement mElement;
    private final List<String> expressions;

    public SQLParser(String sql, ExecutableElement element) {
        this.sql = sql;
        this.mElement = element;
        expressions = new SQLExpressionParser(sql).getExpressions();
    }

    public List<String> expressions() {
        return expressions;
    }

    private VariableElement findParameter(String expression) {
        if (StringUtil.isEmpty(expression)) {
            return null;
        }
        if (!expression.contains(".")) {
            return findByName(expression);
        }
        String varName = expression.split(Pattern.quote("."))[0];
        return findByName(varName);
    }

    private VariableElement findByName(String name) {
        for (VariableElement parameter : mElement.getParameters()) {
            if (parameter.getSimpleName().contentEquals(name)) {
                return parameter;
            }
        }
        return null;
    }

    public TypeMirror findType(String expression) {
        VariableElement parameter = findParameter(expression);
        if (parameter == null) {
            return null;
        }
        if (!expression.contains(".")) {
            return parameter.asType();
        }

        String[] paragraphs = expression.split(Pattern.quote("."));
        List<? extends Element> enclosedElements =
                getEnclosedElements(parameter);
        TypeMirror iter = null;
        for (int i = 1; i < paragraphs.length; i++) {
            // iterate for next expression
            String paragraphExpression = paragraphs[i];
            if (paragraphExpression.isEmpty()) {
                return null;
            }
            if (!paragraphExpression.contains("(")) {
                // is field
                VariableElement element = findFieldSimpleName(
                        enclosedElements,
                        paragraphExpression);
                if (element == null) {
                    return null;
                }
                enclosedElements = getEnclosedElements(element);
                iter = element.asType();
            } else {
                // method
                String removeBrackets = paragraphExpression
                        .replaceAll(Pattern.quote("("), "")
                        .replaceAll(Pattern.quote(")"), "");
                // only try to find method with no parameters
                TypeMirror mirror = findMethodReturnsBySimpleName(
                        enclosedElements, removeBrackets);
                if (mirror == null) {
                    return null;
                }
                iter = mirror;
                TypeElement element = ElementUtil.asTypeElement(iter);
                if (element == null) {
                    break;
                }
                enclosedElements = element.getEnclosedElements();
            }
        }
        return iter;
    }

    private List<? extends Element> getEnclosedElements(VariableElement element) {
        // call getEnclosedElements on VariableElement will get an empty list.
        // But we want to get all members of this type.
        TypeElement typeElement =
                ElementUtil.asTypeElement(element.asType());
        if (typeElement != null) {
            return typeElement.getEnclosedElements();
        }
        return Collections.emptyList();
    }

    private VariableElement findFieldSimpleName(List<? extends Element> enclosedElements, String simpleName) {
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement.getKind().isField() &&
                    enclosedElement.getSimpleName().contentEquals(simpleName)) {
                return (VariableElement) enclosedElement;
            }
        }
        return null;
    }

    private TypeMirror findMethodReturnsBySimpleName(List<? extends Element> enclosedElements,
                                                     String simpleName) {
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement.getKind() == ElementKind.METHOD &&
                    enclosedElement.getSimpleName().contentEquals(simpleName)) {
                ExecutableElement element = (ExecutableElement) enclosedElement;
                if (element.getParameters().isEmpty()) {
                    return element.getReturnType();
                }
            }
        }
        return null;
    }
}
