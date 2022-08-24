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

package space.lingu.light.compile;

/**
 * Light编译时错误
 *
 * @author RollW
 */
public class LightCompileException extends RuntimeException {
    public LightCompileException() {
        super();
    }

    public LightCompileException(String message) {
        super(message);
    }

    public LightCompileException(String message, Throwable cause) {
        super(message, cause);
    }

    public LightCompileException(Throwable cause) {
        this(CompileErrors.BUG_REPORT, cause);
    }

    protected LightCompileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
