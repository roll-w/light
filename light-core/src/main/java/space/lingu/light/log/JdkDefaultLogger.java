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

package space.lingu.light.log;

import space.lingu.light.LightLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Jdk util logging.
 *
 * @author RollW
 */
public class JdkDefaultLogger implements LightLogger {
    private static final String THROWABLE_INFO = " Throwable info: \n";
    private static final LightLogger GLOBAL_LOGGER = new JdkDefaultLogger(Logger.getGlobal());

    private final Logger mJdkLogger;
    private boolean isDebug = false;


    public static LightLogger getLogger(String name) {
        return new JdkDefaultLogger(Logger.getLogger(name));
    }

    public static LightLogger getGlobalLogger() {
        return GLOBAL_LOGGER;
    }

    private JdkDefaultLogger(Logger jdkLogger) {
        mJdkLogger = jdkLogger;
    }

    @Override
    public boolean isDebugEnabled() {
        return isDebug;
    }

    @Override
    public void setDebugEnable(boolean isDebug) {
        this.isDebug = isDebug;
    }

    @Override
    public void debug(String message) {
        if (!isDebugEnabled()) return;
        mJdkLogger.log(Level.INFO, message);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        if (!isDebugEnabled()) return;
        mJdkLogger.log(Level.INFO, message + THROWABLE_INFO + throwable.getMessage());
    }

    @Override
    public void error(String message) {
        mJdkLogger.log(Level.SEVERE, message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        mJdkLogger.log(Level.SEVERE, message + THROWABLE_INFO + throwable.getMessage());
    }

    @Override
    public void error(Throwable throwable) {
        mJdkLogger.log(Level.SEVERE, THROWABLE_INFO + throwable.getMessage());
    }

    @Override
    public void info(String message) {
        mJdkLogger.log(Level.INFO, message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        mJdkLogger.log(Level.INFO, message + THROWABLE_INFO + throwable.getMessage());
    }

    @Override
    public void trace(String message) {
        mJdkLogger.log(Level.FINE, message);
    }

    @Override
    public void trace(String message, Throwable throwable) {
        mJdkLogger.log(Level.FINE, message + THROWABLE_INFO + LightLogger.formatStackTraces(throwable.getStackTrace()));
    }

    @Override
    public void trace(Throwable throwable) {
        mJdkLogger.log(Level.FINE, "Throwable info: " + LightLogger.formatStackTraces(throwable.getStackTrace()));
    }

    @Override
    public void warn(String message) {
        mJdkLogger.log(Level.WARNING, message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        mJdkLogger.log(Level.WARNING, message + THROWABLE_INFO + throwable.getMessage());
    }
}
