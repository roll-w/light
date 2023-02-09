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

package space.lingu.light;

import space.lingu.light.connect.ConnectionPool;
import space.lingu.light.sql.DialectProvider;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Light util class.
 *
 * @author RollW
 */
public final class Light {

    public static <T extends LightDatabase> LightDatabase.Builder<T> databaseBuilder(
            Class<T> clazz, Class<? extends DialectProvider> providerClass) {
        return new LightDatabase.Builder<>(clazz, providerClass);
    }

    public static <T extends DialectProvider> T createDialectProviderInstance(final Class<T> clazz) {
        T provider;
        try {
            Constructor<T> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            provider = constructor.newInstance();
            return provider;
        } catch (NoSuchMethodException e) {
            throw new LightRuntimeException("A DialectProvider class must have a parameterless constructor!");
        } catch (InstantiationException e) {
            throw new LightRuntimeException("Failed to create an instance of "
                    + clazz.getCanonicalName());
        } catch (IllegalAccessException e) {
            throw new LightRuntimeException("Cannot access the constructor "
                    + clazz.getCanonicalName());
        } catch (InvocationTargetException e) {
            throw new LightRuntimeException(e);
        }
    }

    public static <T extends ConnectionPool> T createConnectionPoolInstance(final Class<T> clazz) {
        T pool;
        try {
            final Constructor<T> tConstructor = clazz.getConstructor();
            tConstructor.setAccessible(true);
            pool = tConstructor.newInstance();
            return pool;
        } catch (NoSuchMethodException e) {
            throw new LightRuntimeException("A ConnectionPool class must have a parameterless constructor!");
        } catch (InstantiationException e) {
            throw new LightRuntimeException("Failed to create an instance of "
                    + clazz.getCanonicalName());
        } catch (IllegalAccessException e) {
            throw new LightRuntimeException("Cannot access the constructor "
                    + clazz.getCanonicalName());
        } catch (InvocationTargetException e) {
            throw new LightRuntimeException(e);
        }
    }

    public static <T, C> T getGeneratedImplInstance(final Class<C> clazz,
                                                    final String suffix) {
        final String fullPackage = clazz.getPackage().getName();
        String name = clazz.getCanonicalName();
        final String postPackageName = fullPackage.isEmpty()
                ? name : name.substring(fullPackage.length() + 1);
        final String implName = postPackageName.replace('.', '_') + suffix;
        try {
            final String fullClassName = fullPackage.isEmpty()
                    ? implName : fullPackage + "." + implName;
            @SuppressWarnings("unchecked")
            final Class<T> aClass = (Class<T>) Class.forName(
                    fullClassName, true, clazz.getClassLoader());
            Constructor<T> constructor = aClass.getConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ClassNotFoundException e) {
            throw new LightRuntimeException("Cannot find implementation for "
                    + fullPackage + "." + implName + ", class does not exist.");
        } catch (IllegalAccessException e) {
            throw new LightRuntimeException("Cannot access the constructor "
                    + clazz.getCanonicalName());
        } catch (InstantiationException e) {
            throw new LightRuntimeException("Failed to create an instance of "
                    + clazz.getCanonicalName());
        } catch (NoSuchMethodException e) {
            throw new LightRuntimeException("Cannot find a parameterless constructor of "
                    + clazz.getCanonicalName());
        } catch (InvocationTargetException e) {
            throw new LightRuntimeException("Invocation target exception.", e.getTargetException());
        }
    }

    public static InputStream loadResource(String path) {
        return Light.class.getResourceAsStream("/" + path);
    }

    private Light() {
    }
}
