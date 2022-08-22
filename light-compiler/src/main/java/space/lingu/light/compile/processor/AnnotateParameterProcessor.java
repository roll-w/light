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

import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeUtil;
import space.lingu.light.compile.struct.AnnotateParameter;
import space.lingu.light.compile.writer.ClassWriter;
import space.lingu.light.util.Pair;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * @author RollW
 */
public class AnnotateParameterProcessor implements Processor<AnnotateParameter> {
    private final VariableElement mElement;
    private final ProcessEnv mEnv;
    private final TypeElement mContaining;
    private final AnnotateParameter param = new AnnotateParameter();

    public AnnotateParameterProcessor(VariableElement element,
                                      TypeElement containing,
                                      ProcessEnv env) {
        mElement = element;
        mEnv = env;
        mContaining = containing;
    }

    @Override
    public AnnotateParameter process() {
        TypeMirror asContaining = mElement.asType();

        String name = mElement.getSimpleName().toString();
        if (name.startsWith(ClassWriter.CLASS_MEMBER_PREFIX)) {
            mEnv.getLog().error(CompileErrors.PARAM_NON_COMPLIANCE, mElement);
        }
        Pair<TypeElement, Boolean> pair = extractPojo(asContaining);

        return param.setElement(mElement)
                .setName(name)
                .setType((TypeElement) mEnv.getTypeUtils().asElement(asContaining))
                .setTypeMirror(asContaining)
                .setWrappedType(pair.first)
                .setMultiple(pair.second);
    }

    private TypeElement extractTypeFromIterator(TypeElement type, DeclaredType declaredType) {
        List<? extends Element> elements = type.getEnclosedElements();
        for (Element e: elements) {
            if (ElementUtil.isPublic(e) && !ElementUtil.isStatic(e) &&
                    e.getKind() == ElementKind.METHOD &&
                    e.getSimpleName().contentEquals("iterator")) {
                TypeMirror asMember = mEnv.getTypeUtils().asMemberOf(TypeUtil.asDeclared(declaredType), e);
                return ElementUtil.getGenericElements(TypeUtil.asExecutable(asMember).getReturnType()).get(0);
            }
        }
        throw new LightCompileException("iterator() not found in Iterable " + type.getQualifiedName());
    }

    private Pair<TypeElement, Boolean> extractPojo(TypeMirror typeMirror) {
        if (TypeUtil.isArray(typeMirror)) {
            // It must be able to convert to TypeElement,
            // or its parameter types do not follow the rules
            TypeElement inner = ElementUtil.asTypeElement(TypeUtil.getArrayElementType(typeMirror));
            return Pair.createPair(inner, true);
        }

        DeclaredType declaredType = TypeUtil.asDeclared(typeMirror);
        if (declaredType.getTypeArguments() == null || declaredType.getTypeArguments().isEmpty()) {
            return Pair.createPair(ElementUtil.asTypeElement(typeMirror), false);
        }
        TypeElement iterEle = mEnv.getElementUtils().getTypeElement("java.lang.Iterable");
        TypeElement ele = extractTypeFromIterator(iterEle, declaredType);
        return Pair.createPair(ele, true);
    }
}
