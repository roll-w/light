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
import space.lingu.light.compile.javac.CompileType;
import space.lingu.light.compile.javac.MethodCompileType;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.javac.TypeCompileType;
import space.lingu.light.compile.javac.TypeUtils;
import space.lingu.light.compile.javac.VariableCompileType;
import space.lingu.light.compile.struct.AnnotateParameter;
import space.lingu.light.compile.struct.DataTable;
import space.lingu.light.compile.struct.ParamEntity;
import space.lingu.light.compile.struct.Parameter;
import space.lingu.light.compile.struct.Pojo;
import space.lingu.light.util.Pair;

import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A general processor that provides
 * general process for parsing method
 * processors that use annotations directly.
 *
 * @author RollW
 */
public class AnnotateMethodProcessor {
    private final MethodCompileType methodCompileType;
    private final ProcessEnv mEnv;

    public AnnotateMethodProcessor(MethodCompileType methodCompileType,
                                   ProcessEnv env) {
        this.methodCompileType = methodCompileType;
        mEnv = env;
    }

    public List<Parameter> extractRawParameters(TypeCompileType compileType) {
        List<VariableCompileType> parameterTypes = methodCompileType.getParameters();
        List<Parameter> parameters = new ArrayList<>();
        parameterTypes.forEach(e -> {
            checkUnbound(e, mEnv);
            Processor<AnnotateParameter> processor =
                    new AnnotateParameterProcessor(e, compileType, mEnv);
            parameters.add(processor.process());
        });
        return parameters;
    }

    public Pair<Map<String, ParamEntity>, List<Parameter>> extractParameters(TypeCompileType typeCompileType) {
        List<Parameter> parameters = extractRawParameters(typeCompileType);
        Map<String, ParamEntity> entityMap = new HashMap<>(
                extractEntities(parameters));
        return Pair.createPair(entityMap, parameters);
    }

    public Map<String, ParamEntity> extractEntities(List<Parameter> params) {
        final Map<String, ParamEntity> entityMap = new HashMap<>();
        params.forEach(param -> {
            if (param == null) {
                return;
            }

            TypeCompileType entityType = param.getWrappedCompileType();

            if (param.getWrappedCompileType() != null && TypeUtils.equalTypeMirror(
                    param.getCompileType().getType().getTypeMirror(),
                    param.getWrappedCompileType().getTypeMirror()
            )) {
                entityType = param.getCompileType().getType();
            }

            if (entityType == null) {
                mEnv.getLog().error(
                        CompileErrors.DAO_INVALID_METHOD_PARAMETER,
                        methodCompileType
                );
                return;
            }

            // TODO: support fragment type
            Pojo pojo = new PojoProcessor(entityType, mEnv).process();
            if (entityType.getAnnotation(space.lingu.light.DataTable.class) == null) {
                mEnv.getLog().error(CompileErrors.ACTUAL_PARAM_ANNOTATED_DATATABLE, methodCompileType);
                return;
            }
            DataTable dataTable = new DataTableProcessor(
                    param.getWrappedCompileType(),
                    mEnv).process();
            ParamEntity paramEntity = new ParamEntity(dataTable, null);
            entityMap.put(param.getName(), paramEntity);
        });

        return entityMap;
    }

    public static void checkUnbound(CompileType compileType, ProcessEnv env) {
        TypeMirror typeMirror = compileType.getTypeMirror();
        if (!TypeUtils.isIterable(env, typeMirror)) {
            return;
        }
        List<? extends TypeMirror> genericTypes = TypeUtils.getGenericTypes(typeMirror);
        if (genericTypes == null || genericTypes.isEmpty()) {
            env.getLog().error(
                    CompileErrors.NOT_BOUND_GENERIC_TYPES,
                    compileType
            );
        }
    }
}
