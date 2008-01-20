/*
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
package org.fabric3.monitor.impl;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.net.URI;

import org.fabric3.api.annotation.LogLevel;
import org.fabric3.monitor.MonitorFactory;

/**
 * A factory for monitors that forwards events to a {@link java.util.logging.Logger Java Logging (JSR47) Logger}.
 *
 * @version $Rev$ $Date$
 * @see java.util.logging
 */
public class JavaLoggingMonitorFactory implements MonitorFactory {
    private final Properties levels;
    private final Level defaultLevel;
    private final String bundleName;
    private final Map<Class<?>, WeakReference<?>> proxies = new WeakHashMap<Class<?>, WeakReference<?>>();

    /**
     * Construct a MonitorFactory that will monitor the specified methods at the specified levels and generate messages
     * using java.util.logging.
     * <p/>
     * The supplied Properties can be used to specify custom log levels for specific monitor methods. The key should be
     * the method name in form returned by <code>Class.getName() + '#' + Method.getName()</code> and the value the log
     * level to use as defined by {@link java.util.logging.Level}.
     *
     * @param levels       definition of custom levels for specific monitored methods, may be null or empty.
     * @param defaultLevel the default log level to use
     * @param bundleName   the name of a resource bundle that will be passed to the logger
     * @see java.util.logging.Logger
     */
    public JavaLoggingMonitorFactory(Properties levels, Level defaultLevel, String bundleName) {
        this.levels = levels;
        this.defaultLevel = defaultLevel;
        this.bundleName = bundleName;
    }

    public <T> T getMonitor(Class<T> monitorInterface, URI componentId) {
        return getMonitor(monitorInterface);
    }

    public synchronized <T> T getMonitor(Class<T> monitorInterface) {
        T monitor = getCachedMonitor(monitorInterface);
        if (monitor == null) {
            monitor = createMonitor(monitorInterface);
            proxies.put(monitorInterface, new WeakReference<T>(monitor));
        }
        return monitor;
    }

    protected <T> T getCachedMonitor(Class<T> monitorInterface) {
        WeakReference<?> ref = proxies.get(monitorInterface);
        return (ref != null) ? monitorInterface.cast(ref.get()) : null;
    }

    protected <T> T createMonitor(Class<T> monitorInterface) {
        String className = monitorInterface.getName();
        Logger logger = Logger.getLogger(className);
        ResourceBundle bundle = locateBundle(monitorInterface, bundleName);

        Method[] methods = monitorInterface.getMethods();
        Map<Method, MethodInfo> methodInfo = new ConcurrentHashMap<Method, MethodInfo>(methods.length);
        for (Method method : methods) {
            String methodName = method.getName();
            String key = className + '#' + methodName;
            String levelName = null;
            if (levels != null) {
                levelName = levels.getProperty(key);
            }
            if (levelName == null) {
                LogLevel annotation = method.getAnnotation(LogLevel.class);
                if (annotation != null) {
                    levelName = annotation.value();
                }
            }

            Level methodLevel;
            if (levelName == null) {
                methodLevel = defaultLevel;
            } else {
                try {
                    methodLevel = Level.parse(levelName);
                } catch (IllegalArgumentException e) {
                    methodLevel = defaultLevel;
                }
            }

            int throwable = -1;
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Class<?> paramType = method.getParameterTypes()[i];
                if (Throwable.class.isAssignableFrom(paramType)) {
                    throwable = i;
                    break;
                }
            }
            MethodInfo info = new MethodInfo(logger, methodLevel, methodName, bundle, throwable);
            methodInfo.put(method, info);
        }

        InvocationHandler handler = new LoggingHandler(methodInfo);
        Object proxy = Proxy.newProxyInstance(monitorInterface.getClassLoader(),
                                              new Class<?>[]{monitorInterface},
                                              handler);
        return monitorInterface.cast(proxy);
    }

    protected <T> ResourceBundle locateBundle(Class<T> monitorInterface, String bundleName) {
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

    private static class MethodInfo {
        private final Logger logger;
        private final Level level;
        private final String methodName;
        private final ResourceBundle bundle;
        private final int throwable;

        private MethodInfo(Logger logger, Level level, String methodName, ResourceBundle bundle, int throwable) {
            this.logger = logger;
            this.level = level;
            this.methodName = methodName;
            this.bundle = bundle;
            this.throwable = throwable;
        }

        private void invoke(Object[] args) {
            if (level == null || !logger.isLoggable(level)) {
                return;
            }

            // construct the key for the resource bundle
            String className = logger.getName();
            String key = className + '#' + methodName;

            LogRecord logRecord = new LogRecord(level, key);
            logRecord.setLoggerName(className);
            logRecord.setSourceClassName(className);
            logRecord.setSourceMethodName(methodName);
            logRecord.setParameters(args);
            if (args != null && throwable >= 0) {
                logRecord.setThrown((Throwable) args[throwable]);
            }
            logRecord.setResourceBundle(bundle);
            logger.log(logRecord);
        }
    }

    private static class LoggingHandler implements InvocationHandler {
        private final Map<Method, MethodInfo> info;

        public LoggingHandler(Map<Method, MethodInfo> methodInfo) {
            this.info = methodInfo;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            MethodInfo methodInfo = info.get(method);
            if (methodInfo != null) {
                methodInfo.invoke(args);
            }
            return null;
        }
    }

}
