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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import space.lingu.light.compile.javac.TypeUtil;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Constructor
 *
 * @author RollW
 */
public class Constructor {
    private final ExecutableElement element;
    private final List<Field> fields;

    public Constructor(ExecutableElement element, List<Field> fields) {
        this.element = element;
        this.fields = fields;
    }

    public List<Field> getFields() {
        return fields;
    }

    public ExecutableElement getElement() {
        return element;
    }

    public boolean containsField(Field field) {
        List<? extends VariableElement> elements = element.getParameters();
        AtomicBoolean flag = new AtomicBoolean(false);
        elements.forEach(e -> {
            // compare name and typeMirror
            if (e.getSimpleName().contentEquals(field.getName()) &&
                    TypeUtil.equalTypeMirror(e.asType(), field.getTypeMirror())) {
                flag.set(true);
            }
        });

        return flag.get();
    }

    public void writeConstructor(String out, String args, CodeBlock.Builder builder) {
        if (element == null) {
            throw new IllegalStateException("Must set execute element first.");
        }
        if (element.getKind() == ElementKind.CONSTRUCTOR) {
            builder.addStatement("$L = new $T($L)", out, ClassName.get((TypeElement) element.getEnclosingElement()),
                    args);
        } else if (element.getKind() == ElementKind.METHOD) {
            builder.addStatement("$L = $T.$L($L)", out, ClassName.get((TypeElement) element.getEnclosingElement()),
                    element.getSimpleName().toString(), args);
        } else {
            throw new IllegalArgumentException("Invalid constructor for " + element.getKind());
        }
    }
}
