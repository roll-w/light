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
import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.javac.VariableCompileType;
import space.lingu.light.compile.javac.types.JavacTypeCompileType;
import space.lingu.light.handler.SQLExpressionParser;
import space.lingu.light.util.StringUtil;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author RollW
 */
public class SQLParser {
    // TODO
    private final String sql;
    private final MethodCompileType methodCompileType;
    private final List<String> expressions;

    public SQLParser(String sql, MethodCompileType methodCompileType) {
        this.sql = sql;
        this.methodCompileType = methodCompileType;
        this.expressions = new SQLExpressionParser(sql).getExpressions();
    }

    public List<String> expressions() {
        return expressions;
    }

    private VariableCompileType findParameter(String expression) {
        if (StringUtil.isEmpty(expression)) {
            return null;
        }
        if (!expression.contains(".")) {
            return findByName(expression);
        }
        String varName = expression.split(Pattern.quote("."))[0];
        return findByName(varName);
    }

    private VariableCompileType findByName(String name) {
        for (VariableCompileType parameter : methodCompileType.getParameters()) {
            if (parameter.getSimpleName().contentEquals(name)) {
                return parameter;
            }
        }
        return null;
    }

    public TypeCompileType findType(String expression) {
        VariableCompileType parameter = findParameter(expression);
        // TODO: may supports generic types in the future
        if (parameter == null) {
            return null;
        }
        if (!expression.contains(".")) {
            return parameter.getType();
        }

        String[] paragraphs = expression.split(Pattern.quote("."));
        List<? extends Element> enclosedElements =
                getEnclosedElements(parameter.getElement());
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
        if (iter == null) {
            return null;
        }
        return new JavacTypeCompileType(
                iter,
                ElementUtil.asTypeElement(iter)
        );
    }

    private List<? extends Element> getEnclosedElements(VariableElement element) {
        // call getEnclosedElements on VariableElement will get an empty list.
        // But we want to get all members of this type.
        TypeElement typeElement =
                ElementUtil.asTypeElement(element.asType());
        if (typeElement != null) {
            return getAllEnclosedElements(typeElement);
        }
        return Collections.emptyList();
    }

    private List<? extends Element> getAllEnclosedElements(TypeElement element) {
        List<Element> elements = new ArrayList<>(element.getEnclosedElements());
        // may have duplicate elements
        for (TypeElement typeElement : getInterfacesOrSuperClass(element)) {
            elements.addAll(getAllEnclosedElements(typeElement));
        }
        return elements;
    }

    private List<TypeElement> getInterfacesOrSuperClass(TypeElement element) {
        List<TypeMirror> interfaces = new ArrayList<>();
        TypeMirror superClass = element.getSuperclass();
        if (superClass.getKind() != TypeKind.NONE) {
            interfaces.add(superClass);
        }
        interfaces.addAll(element.getInterfaces());
        return interfaces.stream()
                .map(ElementUtil::asTypeElement)
                .collect(Collectors.toList());
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

    public String getSql() {
        return sql;
    }

    public List<String> getExpressions() {
        return expressions;
    }
}
