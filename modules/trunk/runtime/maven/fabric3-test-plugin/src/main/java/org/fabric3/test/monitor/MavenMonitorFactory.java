/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * --- Original Apache License ---
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.fabric3.test.monitor;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.logging.Level;

import org.apache.maven.plugin.logging.Log;

import org.fabric3.api.annotation.logging.LogLevels;
import org.fabric3.host.monitor.MonitorFactory;

/**
 *
 *
 */
public class MavenMonitorFactory implements MonitorFactory {

    private final Log log;
    private final String bundleName;
    private final Level defaultLevel;
    private final Map<Class<?>, WeakReference<?>> proxies = new WeakHashMap<Class<?>, WeakReference<?>>();

    /**
     * @param log
     * @param bundleName
     */
    public MavenMonitorFactory(Log log, String bundleName) {
        this.log = log;
        this.bundleName = bundleName;
        this.defaultLevel = Level.FINEST;
    }

    /**
     *
     */
    public void readConfiguration(URL url) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public synchronized <T> T getMonitor(Class<T> monitorInterface, URI componentId) {
        return getMonitor(monitorInterface);
    }

    /**
     *
     */
    public synchronized <T> T getMonitor(Class<T> monitorInterface) {
        T proxy = getCachedMonitor(monitorInterface);
        if (proxy == null) {
            proxy = createMonitor(monitorInterface);
            proxies.put(monitorInterface, new WeakReference<T>(proxy));
        }
        return proxy;
    }

    private <T> ResourceBundle locateBundle(Class<T> monitorInterface, String bundleName) {

        Locale locale = Locale.getDefault();
        ClassLoader cl = monitorInterface.getClassLoader();
        String packageName = monitorInterface.getPackage().getName();

        while (true) {
            try {
                return ResourceBundle.getBundle(packageName + '.' + bundleName, locale, cl);
            } catch (MissingResourceException e) {
                //ok
            }
            int index = packageName.lastIndexOf('.');
            if (index == -1) {
                break;
            }
            packageName = packageName.substring(0, index);
        }

        try {
            return ResourceBundle.getBundle(bundleName, locale, cl);
        } catch (Exception e) {
            return null;
        }

    }

    private <T> T getCachedMonitor(Class<T> monitorInterface) {

        WeakReference<?> ref = proxies.get(monitorInterface);
        return (ref != null) ? monitorInterface.cast(ref.get()) : null;

    }

    private <T> T createMonitor(Class<T> monitorInterface) {

        Method[] methods = monitorInterface.getMethods();
        Handler handler = new Handler();

        for (Method method : methods) {
            LogLevels level = LogLevels.getAnnotatedLogLevel(method);
            int value = translateLogLevel(level).intValue();
            int throwable = getExceptionParameterIndex(method);

            String message = getMessage(monitorInterface, method);
            handler.addMethodHandler(method, log, message, value, throwable);
        }

        Object proxy = Proxy.newProxyInstance(monitorInterface.getClassLoader(), new Class<?>[]{monitorInterface}, handler);
        return monitorInterface.cast(proxy);

    }

    private String getMessage(Class<?> monitorInterface, Method method) {

        ResourceBundle bundle = locateBundle(monitorInterface, bundleName);
        String key = monitorInterface.getName() + '#' + method.getName();
        String message = null;

        if (bundle != null) {
            try {
                message = bundle.getString(key);
            } catch (MissingResourceException e) {
                // drop through
            }
        }

        if (message == null) {
            StringBuilder builder = new StringBuilder();
            builder.append(key);
            int argCount = method.getParameterTypes().length;
            if (argCount > 0) {
                builder.append(": {0}");
                for (int i = 1; i < argCount; i++) {
                    builder.append(' ');
                    builder.append('{');
                    builder.append(Integer.toString(i));
                    builder.append('}');
                }
            }
            message = builder.toString();
        }

        return message;

    }

    private Level translateLogLevel(LogLevels level) {

        Level result = null;
        if (level == null) {
            result = defaultLevel;
        } else {
            try {
                //Because the LogLevels' values are based on the Level's logging levels, 
                //no translation is required, just a pass-through mapping
                result = Level.parse(level.toString());
            } catch (IllegalArgumentException e) {
                //TODO: Add error reporting for unsupported log level
                result = defaultLevel;
            }
        }

        return result;

    }

    private int getExceptionParameterIndex(Method method) {

        int result = -1;
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Class<?> paramType = method.getParameterTypes()[i];
            if (Throwable.class.isAssignableFrom(paramType)) {
                result = i;
                break;
            }
        }
        return result;

    }

}
