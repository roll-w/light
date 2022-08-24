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

import space.lingu.light.compile.coder.custom.binder.QueryParameterBinder;

/**
 * @author RollW
 */
public class DeleteParameter extends AnnotateParameter implements SQLCustomParameter{
    private QueryParameterBinder binder;

    public DeleteParameter() {

    }

    public DeleteParameter(Parameter parameter) {
        setName(parameter.getName());
        setType(parameter.getType());
        setWrappedType(parameter.getWrappedType());
        setTypeMirror(parameter.getTypeMirror());
        setMultiple(parameter.isMultiple());
    }

    @Override
    public QueryParameterBinder getBinder() {
        return binder;
    }

    public DeleteParameter setBinder(QueryParameterBinder binder) {
        this.binder = binder;
        return this;
    }

}
