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

import space.lingu.light.OnConflictStrategy;
import space.lingu.light.compile.coder.annotated.binder.AutoDeleteUpdateMethodBinder;
import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.TypeCompileType;

import java.util.List;
import java.util.Map;

/**
 * @author RollW
 */
public class UpdateMethod implements AnnotatedMethod<Parameter> {
    private final MethodCompileType methodCompileType;
    private final Map<String, ParamEntity> entities;
    private final List<Parameter> parameters;
    private final AutoDeleteUpdateMethodBinder binder;
    private final OnConflictStrategy onConflict;

    public UpdateMethod(MethodCompileType methodCompileType,
                        Map<String, ParamEntity> entities,
                        List<Parameter> parameters,
                        AutoDeleteUpdateMethodBinder binder,
                        OnConflictStrategy onConflict) {
        this.methodCompileType = methodCompileType;
        this.entities = entities;
        this.parameters = parameters;
        this.binder = binder;
        this.onConflict = onConflict;
    }

    @Override
    public MethodCompileType getMethodCompileType() {
        return methodCompileType;
    }

    @Override
    public Map<String, ParamEntity> getEntities() {
        return entities;
    }

    @Override
    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public TypeCompileType getReturnType() {
        return methodCompileType.getReturnType();
    }

    @Override
    public AutoDeleteUpdateMethodBinder getBinder() {
        return binder;
    }

    public OnConflictStrategy getOnConflict() {
        return onConflict;
    }
}
