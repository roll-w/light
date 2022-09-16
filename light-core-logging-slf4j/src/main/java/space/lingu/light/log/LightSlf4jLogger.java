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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.lingu.light.LightLogger;

/**
 * slf4j implementation of {@link LightLogger}.
 * <p>
 * If you are using a different version of slf4j-api or
 * just don't want to import any more dependencies,
 * you can simply copy this file to your project.
 * <p>
 * In most cases it will work fine.
 *
 * @author RollW
 */
@SuppressWarnings("unused")
public class LightSlf4jLogger implements LightLogger {
    public static LightLogger createLogger(String name) {
        return new LightSlf4jLogger(LoggerFactory.getLogger(name));
    }

    public static LightLogger createLogger(Class<?> clazz) {
        return new LightSlf4jLogger(LoggerFactory.getLogger(clazz));
    }

    private final Logger logger;

    public LightSlf4jLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /**
     * For third-party logger, you need configure the logger itself.
     *
     * @param isDebug not used
     */
    @Override
    public void setDebugEnable(boolean isDebug) {
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        logger.debug(message, throwable);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public void error(Throwable throwable) {
        logger.error(throwable.toString(), throwable);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        logger.info(message, throwable);
    }

    @Override
    public void trace(String message) {
        logger.trace(message);
    }

    @Override
    public void trace(String message, Throwable throwable) {
        logger.trace(message, throwable);
    }

    @Override
    public void trace(Throwable throwable) {
        logger.trace(throwable.toString(), throwable);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }
}
