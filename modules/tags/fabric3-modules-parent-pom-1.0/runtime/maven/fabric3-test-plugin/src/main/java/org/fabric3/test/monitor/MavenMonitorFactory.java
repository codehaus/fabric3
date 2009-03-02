/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ñLicenseî), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an ñas isî basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
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
