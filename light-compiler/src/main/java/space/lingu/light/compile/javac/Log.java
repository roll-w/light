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

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * 编译时输出日志
 * @author RollW
 */
public class Log {
    private final Messager mMessager;

    public Log(Messager messager) {
        mMessager = messager;
    }

    public void log(Diagnostic.Kind kind, CharSequence charSequence) {
        mMessager.printMessage(kind, charSequence);
    }

    public void log(Diagnostic.Kind kind, CharSequence charSequence,
                    Element element) {
        mMessager.printMessage(kind, charSequence, element);
    }

    public void log(Diagnostic.Kind kind, CharSequence msg,
                    Element e, AnnotationMirror a) {
        mMessager.printMessage(kind, msg, e, a);
    }

    public void log(Diagnostic.Kind kind, CharSequence msg,
                    Element e, AnnotationMirror a,
                    AnnotationValue v) {
        mMessager.printMessage(kind, msg, e, a, v);
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

    public void error(CharSequence s, Element element) {
        log(Diagnostic.Kind.ERROR, s, element);
    }

    public void note(CharSequence s, Element element) {
        log(Diagnostic.Kind.NOTE, s, element);
    }

    public void warn(CharSequence s, Element element) {
        log(Diagnostic.Kind.WARNING, s, element);
    }
}
