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

package space.lingu.light.compile.struct;

import space.lingu.light.compile.coder.annotated.binder.DeleteUpdateMethodBinder;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;

/**
 * @author RollW
 */
public class DeleteMethod extends AnnotatedMethod implements Method<AnnotateParameter> {
    private ExecutableElement element;
    private Map<String, ParamEntity> entities;
    private TypeMirror returnType;
    private List<AnnotateParameter> parameters;
    private DeleteUpdateMethodBinder binder;

    public DeleteMethod() {
    }

    @Override
    public ExecutableElement getElement() {
        return element;
    }

    public DeleteMethod setElement(ExecutableElement element) {
        this.element = element;
        return this;
    }

    @Override
    public Map<String, ParamEntity> getEntities() {
        return entities;
    }

    public DeleteMethod setEntities(Map<String, ParamEntity> entities) {
        this.entities = entities;
        return this;
    }

    @Override
    public TypeMirror getReturnType() {
        return returnType;
    }

    public DeleteMethod setReturnType(TypeMirror returnType) {
        this.returnType = returnType;
        return this;
    }

    @Override
    public List<AnnotateParameter> getParameters() {
        return parameters;
    }

    public DeleteMethod setParameters(List<AnnotateParameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    @Override
    public DeleteUpdateMethodBinder getBinder() {
        return binder;
    }

    public DeleteMethod setBinder(DeleteUpdateMethodBinder binder) {
        this.binder = binder;
        return this;
    }
}
