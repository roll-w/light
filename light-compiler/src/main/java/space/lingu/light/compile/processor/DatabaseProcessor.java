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
import space.lingu.light.Configurations;
import space.lingu.light.Dao;
import space.lingu.light.DataConverters;
import space.lingu.light.LightInfo;
import space.lingu.light.compile.CompileErrors;
import space.lingu.light.compile.javac.ElementUtil;
import space.lingu.light.compile.javac.ProcessEnv;
import space.lingu.light.compile.struct.Configurable;
import space.lingu.light.compile.struct.DataConverter;
import space.lingu.light.compile.struct.DataTable;
import space.lingu.light.compile.struct.Database;
import space.lingu.light.compile.struct.DatabaseDaoMethod;
import space.lingu.light.compile.writer.ClassWriter;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author RollW
 */
public class DatabaseProcessor implements Processor<Database> {
    private final TypeElement mElement;
    private final Database database = new Database();
    private final space.lingu.light.Database anno;
    private final ProcessEnv mEnv;
    private final List<? extends Element> enclosedElements;

    public DatabaseProcessor(TypeElement element, ProcessEnv env) {
        mElement = element;
        anno = mElement.getAnnotation(space.lingu.light.Database.class);
        mEnv = env;
        List<Element> elements = new ArrayList<>();
        getAllSuperClass(element).forEach(clzElement ->
                elements.addAll(clzElement.getEnclosedElements()));
        enclosedElements = elements
                .stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Database process() {
        if (anno.name().isEmpty()) {
            mEnv.getLog().error(CompileErrors.DATABASE_NAME_EMPTY, mElement);
        }
        ClassName superClass = ClassName.get(mElement);
        String packageName = superClass.packageName();
        String implName = superClass.simpleName() + ClassWriter.CLASS_SUFFIX;
        List<DataConverter> dataConverterList = getDataConverterMethods();
        mEnv.getBinders().registerDataConverters(dataConverterList);

        List<TypeMirror> tableClassMirror = new ArrayList<>();
        try {
            Class<?>[] tableClasses = anno.tables();
        } catch (MirroredTypesException e) {
            // it will and should be caught
            tableClassMirror.addAll(e.getTypeMirrors());
        }
        Configurations configurations = Configurable.createFrom(anno.configuration(), mElement);

        return database.setDataTableList(processDataTables(tableClassMirror))
                .setSuperClassElement(mElement)
                .setImplName(implName)
                .setConfigurations(configurations)
                .setDatabaseDaoMethods(getAllDaoMethods(configurations))
                .setImplClassName(
                        ClassName.get(packageName, implName));
    }

    private TypeElement loadLightInfoTable() {
        return mEnv.getElementUtils().getTypeElement(LightInfo.class.getCanonicalName());
    }

    private List<DataTable> processDataTables(List<? extends TypeMirror> mirrors) {
        if (mirrors == null) {
            throw new IllegalArgumentException("Cannot get data table classes");
        }
        List<DataTable> dataTableList = new ArrayList<>();
        mirrors.forEach(typeMirror -> {
            TypeElement element = ElementUtil.asTypeElement(typeMirror);
            if (element == null) {
                mEnv.getLog().error(
                        CompileErrors.DATA_TABLE_NOT_CLASS,
                        mElement);
                return;
            }
            dataTableList.add(new DataTableProcessor(element, mEnv).process());
        });
        dataTableList.add(new DataTableProcessor(loadLightInfoTable(), mEnv).process());

        Set<String> nameSet = new HashSet<>();
        dataTableList.forEach(dataTable -> {
            if (nameSet.contains(dataTable.getTableName())) {
                mEnv.getLog().error(
                        CompileErrors.duplicatedTableName(dataTable.getTableName()),
                        dataTable.getElement()
                );
                return;
            }
            nameSet.add(dataTable.getTableName());
        });

        return dataTableList;
    }

    private List<DataConverter> getDataConverterMethods() {
        DataConverters dataConvertersAnno = mElement.getAnnotation(DataConverters.class);
        List<DataConverter> dataConverterList = new ArrayList<>();
        if (dataConvertersAnno == null) {
            return Collections.emptyList();
        }

        List<? extends TypeMirror> convertersClassMirrors = Collections.emptyList();
        try {
            Class<?>[] classes = dataConvertersAnno.value();
        } catch (MirroredTypesException e) {
            convertersClassMirrors = e.getTypeMirrors();
        }
        convertersClassMirrors.forEach(typeMirror -> {
            TypeElement convertersElement = ElementUtil.asTypeElement(typeMirror);
            if (convertersElement == null) {
                mEnv.getLog().error(
                        CompileErrors.ILLEGAL_DATA_CONVERTERS_CLASS,
                        mElement
                );
                return;
            }

            convertersElement.getEnclosedElements().forEach(enclosedElement -> {
                if (enclosedElement.getAnnotation(space.lingu.light.DataConverter.class) == null) {
                    return;
                }
                if (enclosedElement.getKind() == ElementKind.METHOD) {
                    Processor<DataConverter> converterProcessor =
                            new DataConverterProcessor(
                                    (ExecutableElement) enclosedElement,
                                    convertersElement, mEnv);
                    dataConverterList.add(converterProcessor.process());
                }
            });
        });

        checkRepeatConverters(dataConverterList);
        return dataConverterList;
    }

    private void checkRepeatConverters(List<DataConverter> dataConverters) {
        checkRepeatedInternal(dataConverters, DataConverter::getFromType);
        checkRepeatedInternal(dataConverters, DataConverter::getToType);
    }

    private void checkRepeatedInternal(List<DataConverter> dataConverters, Function<DataConverter, TypeMirror> classifier) {
        dataConverters
                .stream()
                .collect(Collectors.groupingBy(classifier,
                        Collectors.toList()))
                .forEach((type, converters) -> {
                    if (converters.size() <= 1) {
                        return;
                    }
                    converters.forEach(dataConverter -> {
                        List<DataConverter> conflicts = converters.stream()
                                .filter(converter -> converter != dataConverter &&
                                        mEnv.getTypeUtils().isSameType(converter.getFromType(), dataConverter.getFromType()) &&
                                        mEnv.getTypeUtils().isSameType(converter.getToType(), dataConverter.getToType()))
                                .collect(Collectors.toList());
                        if (conflicts.isEmpty()) {
                            return;
                        }
                        mEnv.getLog().error(
                                CompileErrors.repeatedDataConverters(conflicts),
                                dataConverter.getElement()
                        );
                    });
                });
    }

    private List<DatabaseDaoMethod> getAllDaoMethods(Configurations configurations) {
        List<DatabaseDaoMethod> daoMethods = new ArrayList<>();
        for (Element e : enclosedElements) {
            if (e.getKind() != ElementKind.METHOD || !ElementUtil.isAbstract(e)) {
                continue;
            }

            ExecutableElement method = (ExecutableElement) e;
            TypeElement returnType = (TypeElement) mEnv.getTypeUtils().asElement(method.getReturnType());
            Dao daoAnno = returnType.getAnnotation(Dao.class);
            if (daoAnno == null) {
                mEnv.getLog().error(
                        CompileErrors.DATABASE_ABSTRACT_METHOD_RETURN_TYPE, method
                );
            }
            if (!method.getParameters().isEmpty()) {
                mEnv.getLog().error(CompileErrors.DAO_METHOD_NOT_PARAMLESS, method);
            }

            DaoProcessor daoProcessor = new DaoProcessor(
                    returnType, mEnv, configurations);
            space.lingu.light.compile.struct.Dao dao = daoProcessor.process();
            DatabaseDaoMethod daoMethod = new DatabaseDaoMethod(method, dao);
            daoMethods.add(daoMethod);
        }

        return daoMethods;
        // 查找所有抽象方法的返回类型
    }

    private List<TypeElement> getAllSuperClass(TypeElement element) {
        List<TypeElement> elements = new ArrayList<>();
        elements.add(element);
        TypeElement iter = element;
        while (iter.getSuperclass() != null) {
            if (iter.getKind() != ElementKind.CLASS) {
                continue;
            }
            TypeElement e = (TypeElement) mEnv.getTypeUtils().asElement(iter.getSuperclass());
            if (e == null) {
                break;
            }
            elements.add(e);
            iter = e;
        }
        return elements;
    }
}
