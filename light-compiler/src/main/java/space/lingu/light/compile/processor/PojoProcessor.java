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

import space.lingu.light.DataColumn;
import space.lingu.light.Embedded;
import space.lingu.light.LightIgnore;
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.Warnings;
import space.lingu.light.compile.javac.ElementUtils;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.javac.TypeUtils;
import space.lingu.light.compile.javac.VariableCompileType;
import space.lingu.light.compile.javac.types.JavacVariableCompileType;
import space.lingu.light.compile.struct.*;

import javax.lang.model.element.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author RollW
 */
public class PojoProcessor implements Processor<Pojo> {
    private final TypeCompileType typeCompileType;
    private final ProcessEnv mEnv;

    public PojoProcessor(TypeCompileType typeCompileType, ProcessEnv env) {
        this.typeCompileType = typeCompileType;
        mEnv = env;
    }

    @Override
    public Pojo process() {
        Field.Fields fields = new Field.Fields(extractFields());
        Constructor constructor = chooseConstructor(fields);

        final List<ExecutableElement> methods = new ArrayList<>();
        final List<? extends Element> elements = typeCompileType.getElement().getEnclosedElements();
        elements.forEach(e -> {
            if (e.getKind() == ElementKind.METHOD) {
                methods.add((ExecutableElement) e);
            }
        });

        setFieldsGetterMethod(fields.getFields(), methods);
        setFieldsSetterMethod(fields.getFields(), methods, constructor);

        return new Pojo(typeCompileType, fields, constructor);
    }

    private List<Field> extractFields() {
        List<? extends Element> elements = typeCompileType.getElement().getEnclosedElements();
        List<Field> fields = new ArrayList<>();
        elements.forEach(e -> {
            if (e.getKind() != ElementKind.FIELD) {
                return;
            }
            boolean hasColumn = e.getAnnotation(DataColumn.class) != null;
            boolean isIgnore = e.getAnnotation(LightIgnore.class) != null;
            if (ElementUtils.isStatic(e)) {
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
                VariableElement variableElement = (VariableElement) e;
                VariableCompileType variableCompileType =
                        new JavacVariableCompileType(
                                variableElement.asType(),
                                variableElement, mEnv
                        );
                Field field = new FieldProcessor(variableCompileType, mEnv).process();
                fields.add(field);
            }
        });
        return fields;
    }

    private void processEmbeddedField(VariableElement element, Embedded embedded) {

    }


    private Constructor chooseConstructor(Field.Fields fields) {
        List<? extends Element> elements = typeCompileType.getElement().getEnclosedElements();
        List<ExecutableElement> candidates = new ArrayList<>();
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CONSTRUCTOR) {
                continue;
            }
            candidates.add((ExecutableElement) element);
        }
        Constructor annotatedConstructor = checkAnnotatedConstructor(candidates, fields);
        if (annotatedConstructor != null) {
            return annotatedConstructor;
        }

        // find from most to least by the number of its parameters
        candidates.sort(Comparator.comparingInt(e -> e.getParameters().size()));
        Collections.reverse(candidates);
        if (candidates.isEmpty()) {
            mEnv.getLog().error(
                    CompileErrors.cannotFoundConstructor(typeCompileType.getName()),
                    typeCompileType
            );
        }
        Constructor constructor = chooseCandidatesConstructors(candidates, fields);
        if (constructor != null) {
            return constructor;
        }
        mEnv.getLog().error(
                CompileErrors.cannotFoundConstructor(typeCompileType.getName()),
                typeCompileType
        );
        return null;
    }

    private Constructor checkAnnotatedConstructor(List<ExecutableElement> candidates,
                                                  Field.Fields fields) {
        List<ExecutableElement> annotated = candidates
                .stream()
                .filter(e -> e.getAnnotation(space.lingu.light.Constructor.class) != null)
                .collect(Collectors.toList());
        if (annotated.isEmpty()) {
            return null;
        }
        if (annotated.size() > 1) {
            mEnv.getLog().error(CompileErrors.MULTIPLE_CONSTRUCTOR_ANNOTATED, typeCompileType);
        }
        Constructor chosen = chooseCandidatesConstructors(annotated, fields);
        if (chosen != null) {
            return chosen;
        }
        mEnv.getLog().error(CompileErrors.CANNOT_MATCH_CONSTRUCTOR, typeCompileType);
        return null;
    }

    private Constructor chooseCandidatesConstructors(List<ExecutableElement> candidates,
                                                     Field.Fields fields) {
        for (ExecutableElement candidate : candidates) {
            Constructor constructor = checkConstructorParams(candidate, fields);
            if (constructor != null) {
                return constructor;
            }
        }
        return null;
    }

    private Constructor checkConstructorParams(ExecutableElement constructorMethod,
                                               Field.Fields fields) {
        List<Field> constructorParams = new ArrayList<>();
        Set<Field> usedFields = new HashSet<>();
        for (VariableElement e : constructorMethod.getParameters()) {
            Field find = null;
            for (Field field : fields.getFields()) {
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
        if (ElementUtils.isPublic(field.getVariableCompileType().getElement())) {
            FieldGetter getter = new FieldGetter(field.getVariableCompileType(),
                    Field.CallType.FIELD, field.getName());
            field.setGetter(getter);
            return;
        }
        Set<String> candidates = field.getterNameCandidate();
        List<ExecutableElement> filteredElements = elements
                .stream()
                .filter(executableElement ->
                        executableElement.getParameters().isEmpty() &&
                                candidates.contains(executableElement.getSimpleName().toString()))
                .collect(Collectors.toList());
        if (filteredElements.isEmpty()) {
            mEnv.getLog().error(
                    CompileErrors.cannotFoundGetter(candidates),
                    field.getVariableCompileType()
            );
        }
        FieldGetter getter = new FieldGetter(
                field.getVariableCompileType(),
                Field.CallType.METHOD,
                filteredElements.get(0).getSimpleName().toString()
        );
        field.setGetter(getter);
    }

    private void setFieldSetterMethod(Field field,
                                      List<ExecutableElement> elements,
                                      Constructor constructor) {
        if (constructor != null && constructor.containsField(field)) {
            FieldSetter setter = new FieldSetter(field.getVariableCompileType(),
                    Field.CallType.CONSTRUCTOR, field.getName());
            field.setSetter(setter);
            return;
        }

        if (ElementUtils.isPublic(field.getVariableCompileType().getElement())) {
            FieldSetter setter = new FieldSetter(field.getVariableCompileType(),
                    Field.CallType.FIELD, field.getName());
            field.setSetter(setter);
            return;
        }
        Set<String> candidates = field.setterNameCandidate();
        List<ExecutableElement> filteredElements = elements
                .stream()
                .filter(
                        executableElement -> candidates.contains(
                                executableElement.getSimpleName().toString()) &&
                                executableElement.getParameters().size() == 1 &&
                                TypeUtils.equalTypeMirror(
                                        executableElement.getParameters().get(0).asType(),
                                        field.getVariableCompileType().getTypeMirror()
                                )
                )
                .collect(Collectors.toList());
        if (filteredElements.isEmpty()) {
            mEnv.getLog().error(
                    CompileErrors.cannotFoundSetter(candidates),
                    field.getVariableCompileType()
            );
        }
        FieldSetter setter = new FieldSetter(
                field.getVariableCompileType(),
                Field.CallType.METHOD,
                filteredElements.get(0).getSimpleName().toString());
        field.setSetter(setter);
    }


}
