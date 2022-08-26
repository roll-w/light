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

package space.lingu.light.compile.writer;

import com.squareup.javapoet.*;
import space.lingu.light.compile.JavaPoetClass;
import space.lingu.light.compile.struct.ParamEntity;
import space.lingu.light.compile.struct.Pojo;

import javax.lang.model.element.Modifier;
import java.util.StringJoiner;

/**
 * @author RollW
 */
public class DeleteHandlerWriter {
    private final ParamEntity mEntity;
    private final String tableName;
    private final Pojo mPojo;

    public DeleteHandlerWriter(ParamEntity entity) {
        mEntity = entity;
        mPojo = entity.getPojo();
        tableName = entity.getTableName();
    }

    public TypeSpec createAnonymous(ClassWriter writer, String dbParam) {
        StringJoiner args = new StringJoiner(", ");
        mEntity.getPrimaryKey().getFields().fields.forEach(field -> {
            args.add("\"" + field.getColumnName() + "\"");
        });
        AnnotatedMethodWriter delegate = new AnnotatedMethodWriter(mPojo);
        TypeSpec.Builder builder = TypeSpec.anonymousClassBuilder("$L", dbParam)
                .superclass(ParameterizedTypeName.get(JavaPoetClass.DELETE_UPDATE_HANDLER, mPojo.getTypeName()))
                .addMethod(
                        MethodSpec.methodBuilder("createQuery")
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(Override.class)
                                .returns(ClassName.get("java.lang", "String"))
                                .addStatement("return $N.getDialectProvider().getGenerator().delete($S, $L)",
                                        DaoWriter.sDatabaseField, tableName, args.toString())
                                .build());

        builder.addMethod(delegate.createBindMethod(writer, mPojo.getFields()));
        return builder.build();
    }
}
