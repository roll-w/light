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
import space.lingu.light.Embedded;
import space.lingu.light.LightIgnore;
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.Warnings;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.*;

import javax.lang.model.element.*;
import java.util.*;
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
            if (e.getKind() != ElementKind.FIELD) {
                return;
            }
            boolean hasColumn = e.getAnnotation(DataColumn.class) != null;
            boolean isIgnore = e.getAnnotation(LightIgnore.class) != null;
            if (ElementUtil.isStatic(e)) {
                if (hasColumn && !isIgnore) {
                    mEnv.getLog().warn(Warnings.CANNOT_APPLY_TO_STATIC_FIELD, e);
                }
                return;
            }
            if (!hasColumn && !isIgnore) {
                mEnv.getLog().warn(Warnings.FIELD_NOT_ANNOTATED, e);
            }
            if (!hasColumn) {
                return;
            }
            Embedded embedded = e.getAnnotation(Embedded.class);
            if (embedded == null) {
                Field field = new FieldProcessor((VariableElement) e, mEnv).process();
                fields.add(field);
                return;
            }


        });
        return fields;
    }

    private void processEmbeddedField(VariableElement element, Embedded embedded) {

    }


    private Constructor chooseConstructor(List<Field> fields) {
        // 从参数数量由少到多寻找
        List<? extends Element> elements = mElement.getEnclosedElements();

        List<ExecutableElement> constructorsForChoose = new ArrayList<>();
        elements.forEach(e -> {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                constructorsForChoose.add((ExecutableElement) e);
            }
        });

        constructorsForChoose.sort(Comparator.comparingInt(o ->
                o.getParameters().size()));

        if (constructorsForChoose.isEmpty()) {
            mEnv.getLog().error(CompileErrors.CANNOT_FOUND_CONSTRUCTOR, mElement);
        }

        for (ExecutableElement element : constructorsForChoose) {
            Constructor constructor = checkConstructorParams(element, fields);
            if (constructor != null) {
                return constructor;
            }
        }

        mEnv.getLog().error(CompileErrors.CANNOT_FOUND_CONSTRUCTOR, mElement);
        return null;
    }

    private Constructor checkConstructorParams(ExecutableElement constructorMethod,
                                               List<Field> fields) {
        List<Field> constructorParams = new ArrayList<>();
        Set<Field> usedFields = new HashSet<>();
        for (VariableElement e : constructorMethod.getParameters()) {
            Field find = null;
            for (Field field : fields) {
                if (usedFields.contains(field)) {
                    continue;
                }
                if (field.getPossibleCandidateName()
                        .contains(e.getSimpleName().toString())) {
                    usedFields.add(field);
                    find = field;
                    break;
                }

            }
            if (find == null) {
                return null;
            }
            constructorParams.add(find);
        }
        return new Constructor(constructorMethod, constructorParams);
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
        List<ExecutableElement> filteredElements = elements
                .stream()
                .filter(executableElement ->
                        executableElement.getParameters().isEmpty() &&
                                candidates.contains(executableElement.getSimpleName().toString()))
                .collect(Collectors.toList());
        if (filteredElements.isEmpty()) {
            mEnv.getLog().error(
                    CompileErrors.cannotFoundGetter(candidates),
                    field.getElement()
            );
        }
        FieldGetter getter = new FieldGetter(
                field.getElement(),
                Field.CallType.METHOD,
                filteredElements.get(0).getSimpleName().toString()
        );
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
        List<ExecutableElement> filteredElements = elements
                .stream()
                .filter(
                        executableElement -> candidates.contains(
                                executableElement.getSimpleName().toString()) &&
                                executableElement.getParameters().size() == 1 &&
                                Objects.equals(
                                        executableElement.getParameters().get(0).asType(),
                                        field.getTypeMirror()))
                .collect(Collectors.toList());
        if (filteredElements.isEmpty()) {
            mEnv.getLog().error(
                    CompileErrors.cannotFoundSetter(candidates),
                    field.getElement()
            );
        }
        FieldSetter setter = new FieldSetter(
                field.getElement(),
                Field.CallType.METHOD,
                filteredElements.get(0).getSimpleName().toString());
        field.setSetter(setter);
    }


}
