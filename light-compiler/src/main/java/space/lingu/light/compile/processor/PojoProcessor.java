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

package space.lingu.light.compile.processor;

import com.squareup.javapoet.ClassName;
import space.lingu.light.DataColumn;
import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeUtil;
import space.lingu.light.compile.struct.*;

import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author RollW
 */
public class PojoProcessor implements Processor<Pojo> {
    private final TypeElement mElement;
    private final ProcessEnv mEnv;
    private final Pojo pojo = new Pojo();

    public PojoProcessor(TypeElement element, ProcessEnv env) {
        mElement = element;
        mEnv = env;
    }

    @Override
    public Pojo process() {
        pojo.setElement(mElement)
                .setTypeName(ClassName.get(mElement))
                .setFields(extractFields())
                .setConstructor(chooseConstructor(pojo.getFields()));
        final List<ExecutableElement> methods = new ArrayList<>();
        final List<? extends Element> elements = mElement.getEnclosedElements();
        elements.forEach(e -> {
            if (e.getKind() == ElementKind.METHOD) {
                methods.add((ExecutableElement) e);
            }
        });

        setFieldsGetterMethod(pojo.getFields(), methods);
        setFieldsSetterMethod(pojo.getFields(), methods, pojo.getConstructor());

        return pojo;
    }

    private List<Field> extractFields() {
        List<? extends Element> elements = mElement.getEnclosedElements();
        List<Field> fields = new ArrayList<>();
        elements.forEach(e -> {
            // 只选择包括DataColumn注解的
            if (e.getKind() != ElementKind.FIELD || e.getAnnotation(DataColumn.class) == null) {
                return;
            }
            Field field = new FieldProcessor((VariableElement) e, mEnv).process();
            fields.add(field);
        });
        return fields;
    }

    private Constructor chooseConstructor(List<Field> fields) {
        // 从参数数量由低到高寻找
        List<? extends Element> elements = mElement.getEnclosedElements();
        Constructor constructor = new Constructor();
        List<ExecutableElement> constructorsForChoose = new ArrayList<>();
        elements.forEach(e -> {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                constructorsForChoose.add((ExecutableElement) e);
            }
        });

        constructorsForChoose.sort((o1, o2) -> {
            if (o1.getParameters().size() == o2.getParameters().size()) return 0;
            else return Integer.compare(o1.getParameters().size(), o2.getParameters().size());
        });

        if (constructorsForChoose.isEmpty()) {
            throw new LightCompileException("Cannot find a constructor.");
        }

        for (ExecutableElement element : constructorsForChoose) {
            constructor.setElement(element);
            if (checkConstructorParams(element.getParameters(), fields, constructor)) {
                break;
            }
        }

        return constructor;
    }

    private boolean checkConstructorParams(List<? extends VariableElement> elements,
                                           List<Field> fields,
                                           Constructor constructor) {
        List<Field> constructorParams = new ArrayList<>();
        constructor.setFields(constructorParams);
        for (VariableElement e : elements) {
            for (Field field : fields) {
                if (!field.getPossibleCandidateName()
                        .contains(e.getSimpleName().toString())) {
                    return false;
                }
                constructorParams.add(field);
            }
        }
        return true;
    }

    private void setFieldsGetterMethod(List<Field> fields, List<ExecutableElement> elements) {
        fields.forEach(f -> setFieldGetterMethod(f, elements));
    }

    private void setFieldsSetterMethod(List<Field> fields, List<ExecutableElement> elements, Constructor constructor) {
        fields.forEach(f -> setFieldSetterMethod(f, elements, constructor));
    }

    private void setFieldGetterMethod(Field field, List<ExecutableElement> elements) {
        if (ElementUtil.isPublic(field.getElement())) {
            FieldGetter getter = new FieldGetter(field.getElement(),
                    Field.CallType.FIELD, field.getName());
            field.setGetter(getter);
            return;
        }
        List<String> candidates = field.getterNameCandidate();
        List<ExecutableElement> filteredElements = elements.stream().filter(executableElement -> executableElement.getParameters().isEmpty() &&
                candidates.contains(executableElement.getSimpleName().toString())).collect(Collectors.toList());
        if (filteredElements.isEmpty()) {
            throw new LightCompileException("The getter method of the field cannot be found. " +
                    "Please check whether its name conforms to the rules, " +
                    "or it is a private method, or the return type is different from the field.");
        }
        FieldGetter getter = new FieldGetter(field.getElement(),
                Field.CallType.METHOD, filteredElements.get(0).getSimpleName().toString());
        field.setGetter(getter);
    }

    private void setFieldSetterMethod(Field field,
                                      List<ExecutableElement> elements,
                                      Constructor constructor) {
        if (constructor != null && constructor.containsField(field)) {
            FieldSetter setter = new FieldSetter(field.getElement(),
                    Field.CallType.CONSTRUCTOR, field.getName());
            field.setSetter(setter);
            return;
        }

        if (ElementUtil.isPublic(field.getElement())) {
            FieldSetter setter = new FieldSetter(field.getElement(),
                    Field.CallType.FIELD, field.getName());
            field.setSetter(setter);
            return;
        }
        List<String> candidates = field.setterNameCandidate();
        List<ExecutableElement> filteredElements = elements.stream().filter(executableElement ->
                candidates.contains(executableElement.getSimpleName().toString()) &&
                        executableElement.getParameters().size() == 1 &&
                        TypeUtil.equalTypeMirror(executableElement.getParameters().get(0).asType(),
                                field.getTypeMirror())).collect(Collectors.toList());
        if (filteredElements.isEmpty()) {
            throw new LightCompileException("Cannot find a setter method for field, please check if its name follow rules" +
                    " or is a private method. Candidates: " + candidates);
        }
        FieldSetter setter = new FieldSetter(field.getElement(),
                Field.CallType.METHOD, filteredElements.get(0).getSimpleName().toString());
        field.setSetter(setter);
    }


}
