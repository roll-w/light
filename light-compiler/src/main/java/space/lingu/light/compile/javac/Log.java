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

package space.lingu.light.compile.javac;

import space.lingu.light.compile.LightCompileException;
import space.lingu.light.compile.Warnings;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * Log at compile time
 *
 * @author RollW
 */
public class Log {
    private final Messager messager;

    public Log(Messager messager) {
        this.messager = messager;
    }

    public void log(Diagnostic.Kind kind, CharSequence charSequence) {
        messager.printMessage(kind, "Light: " + charSequence);
    }

    public void log(Diagnostic.Kind kind, CharSequence charSequence,
                    Element element) {
        messager.printMessage(kind, "Light: " + charSequence, element);
    }

    public void log(Diagnostic.Kind kind, CharSequence charSequence,
                    CompileType compileType) {
        messager.printMessage(kind, "Light: " + charSequence,
                compileType.getElement());
    }

    public void log(Diagnostic.Kind kind, CharSequence msg,
                    Element e, AnnotationMirror a) {
        messager.printMessage(kind, "Light: " + msg, e, a);
    }

    public void log(Diagnostic.Kind kind, CharSequence msg,
                    Element e, AnnotationMirror a,
                    AnnotationValue v) {
        messager.printMessage(kind, "Light: " + msg, e, a, v);
    }

    public void error(CharSequence s) {
        log(Diagnostic.Kind.ERROR, s);
    }

    public void note(CharSequence s) {
        log(Diagnostic.Kind.NOTE, s);
    }

    public void warn(CharSequence s) {
        log(Diagnostic.Kind.WARNING, s);
    }

    public void warn(boolean condition, Warnings.Warning warning,
                     Element element, Object... args) {
        if (!condition) {
            return;
        }
        if (Warnings.isIgnored(warning, element)) {
            return;
        }
        warn(warning, element, args);
    }

    public void warn(boolean condition, Warnings.Warning warning,
                     CompileType compileType, Object... args) {
        if (!condition) {
            return;
        }
        if (Warnings.isIgnored(warning, compileType)) {
            return;
        }
        warn(warning, compileType, args);
    }

    public void warn(Warnings.Warning warning) {
        log(Diagnostic.Kind.WARNING, warning.getValue());
    }

    private void warn(Warnings.Warning warning, Element element, Object... args) {
        log(Diagnostic.Kind.WARNING, warning.getValue(args), element);
    }

    private void warn(Warnings.Warning warning, CompileType compileType, Object... args) {
        log(Diagnostic.Kind.WARNING, warning.getValue(args), compileType);
    }

    public void error(CharSequence s, Element element) {
        error(s, element, true);
    }

    public void error(boolean condition, CharSequence s, Element element) {
        if (!condition) {
            return;
        }
        error(s, element, true);
    }

    public void error(CharSequence s, CompileType compileType) {
        error(s, compileType, true);
    }

    public void error(boolean condition, CharSequence s, CompileType compileType) {
        if (!condition) {
            return;
        }
        error(s, compileType, true);
    }

    public void error(CharSequence s, Element element, boolean throwsException) {
        log(Diagnostic.Kind.ERROR, s, element);
        if (throwsException) {
            throw new LightCompileException(s.toString());
        }
    }

    public void error(CharSequence s, CompileType compileType,
                      boolean throwsException) {
        log(Diagnostic.Kind.ERROR, s, compileType);
        if (throwsException) {
            throw new LightCompileException(s.toString());
        }
    }

    public void note(CharSequence s, CompileType compileType) {
        log(Diagnostic.Kind.NOTE, s, compileType);
    }

    public void warn(CharSequence s, CompileType compileType) {
        log(Diagnostic.Kind.WARNING, s, compileType);
    }

    public void note(CharSequence s, Element element) {
        log(Diagnostic.Kind.NOTE, s, element);
    }

    public void warn(CharSequence s, Element element) {
        log(Diagnostic.Kind.WARNING, s, element);
    }
}
