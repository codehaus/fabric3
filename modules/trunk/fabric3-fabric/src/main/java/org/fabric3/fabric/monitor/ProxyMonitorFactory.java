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
package org.fabric3.fabric.monitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.logging.Level;

import org.fabric3.api.annotation.LogLevel;
import org.fabric3.host.GroupException;
import org.fabric3.host.Fabric3Exception;
import org.fabric3.host.Fabric3RuntimeException;
import org.fabric3.host.monitor.ExceptionFormatter;
import org.fabric3.host.monitor.FormatterRegistry;
import org.fabric3.host.monitor.MonitorFactory;

/**
 * @version $Rev$ $Date$
 */
public abstract class ProxyMonitorFactory implements MonitorFactory, FormatterRegistry {
    protected String bundleName;
    protected final List<ExceptionFormatter> formatters = new ArrayList<ExceptionFormatter>();
    protected final ExceptionFormatter<Throwable> defaultFormatter;
    protected Level defaultLevel;
    protected Map<String, Level> levels;
    private final Map<Class<?>, WeakReference<?>> proxies = new WeakHashMap<Class<?>, WeakReference<?>>();


    protected ProxyMonitorFactory() {
        defaultFormatter = new DefaultExceptionFormatter();
    }

    public void initialize(Map<String, Object> configProperties) {
        if (configProperties == null) {
            return;
        }
        initInternal(configProperties);
    }

    protected void initInternal(Map<String, Object> configProperties) {
        try {
            this.defaultLevel = (Level) configProperties.get("defaultLevel");
            this.bundleName = (String) configProperties.get("bundleName");
            Properties levels = (Properties) configProperties.get("levels");

            this.levels = new HashMap<String, Level>();
            if (levels != null) {
                for (Map.Entry<Object, Object> entry : levels.entrySet()) {
                    String method = (String) entry.getKey();
                    String level = (String) entry.getValue();
                    try {
                        this.levels.put(method, Level.parse(level));
                    } catch (IllegalArgumentException e) {
                        throw new InvalidLevelException(method, level);
                    }
                }
            }
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException(cce.getLocalizedMessage());
        }
    }

    public synchronized <T> T getMonitor(Class<T> monitorInterface) {
        T proxy = getCachedMonitor(monitorInterface);
        if (proxy == null) {
            proxy = createMonitor(monitorInterface);
            proxies.put(monitorInterface, new WeakReference<T>(proxy));
        }
        return proxy;
    }

    protected <T> T getCachedMonitor(Class<T> monitorInterface) {
        WeakReference<?> ref = proxies.get(monitorInterface);
        return (ref != null) ? monitorInterface.cast(ref.get()) : null;
    }

    protected <T> T createMonitor(Class<T> monitorInterface) {
        String className = monitorInterface.getName();
        Method[] methods = monitorInterface.getMethods();
        Map<String, Level> levels = new HashMap<String, Level>(methods.length);
        for (Method method : methods) {
            String key = className + '#' + method.getName();
            Level level = null;
            if (this.levels != null) {
                this.levels.get(key);
            }
            // if not specified the in config properties, look for an annotation on the method
            if (level == null) {
                LogLevel annotation = method.getAnnotation(LogLevel.class);
                if (annotation != null && annotation.value() != null) {
                    try {
                        level = Level.parse(annotation.value());
                    } catch (IllegalArgumentException e) {
                        // bad value, just use the default
                        level = defaultLevel;
                    }
                }
            }
            if (level == null) {
                level = defaultLevel;
            }
            levels.put(method.getName(), level);
        }

        InvocationHandler handler = createInvocationHandler(monitorInterface, levels);
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

    public void register(ExceptionFormatter formatter) {
        formatters.add(formatter);
    }

    public void unregister(ExceptionFormatter formatter) {
        formatters.remove(formatter);
    }

    protected abstract <T> InvocationHandler createInvocationHandler(Class<T> monitorInterface,
                                                                     Map<String, Level> levels);

    protected <T extends Throwable> String formatException(T e) {
        ExceptionFormatter<? super T> formatter = getFormatter(e);
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        formatter.write(pw, e);
        format(pw, e);
        pw.close();
        return writer.toString();
    }

    private <T extends Throwable> ExceptionFormatter<? super T> getFormatter(T e) {
        for (ExceptionFormatter<?> candidate : formatters) {
            if (candidate.canFormat(e.getClass())) {
                return (ExceptionFormatter<? super T>) candidate;
            }
        }
        return defaultFormatter;
    }

    protected void format(PrintWriter writer, Throwable throwable) {
        writer.println(throwable.getClass().getName());
        StackTraceElement[] trace = throwable.getStackTrace();
        for (StackTraceElement aTrace : trace) {
            writer.println("\tat " + aTrace);
        }
        Throwable ourCause = throwable.getCause();

        if (ourCause != null) {
            printStackTraceAsCause(writer, ourCause, trace);
        }
    }

    protected <T extends Throwable> void printStackTraceAsCause(PrintWriter pw,
                                                                T throwable,
                                                                StackTraceElement[] causedTrace) {

        // Compute number of frames in common between this and caused
        StackTraceElement[] trace = throwable.getStackTrace();
        int m = trace.length - 1;
        int n = causedTrace.length - 1;
        while (m >= 0 && n >= 0 && trace[m].equals(causedTrace[n])) {
            m--;
            n--;
        }
        if (throwable instanceof GroupException) {
            GroupException groupException = (GroupException) throwable;
            groupException.appendBaseMessage(pw);
            List<Exception> causes = groupException.getCauses();
            for (Exception cause : causes) {
                if (cause instanceof Fabric3Exception) {
                    Fabric3Exception f3ex = (Fabric3Exception) cause;
                    pw.println("\n"+ f3ex.getIdentifier() + " caused:");
                } else if (cause instanceof Fabric3RuntimeException) {
                    Fabric3RuntimeException f3ex = (Fabric3RuntimeException) cause;
                    pw.println("\n"+ f3ex.getIdentifier() + " caused:");
                } else {
                    pw.println("Caused by:");
                }
                format(pw, cause);
            }

        } else {
            int framesInCommon = trace.length - 1 - m;

            pw.println("Caused by: " + throwable.getClass().getName());

            ExceptionFormatter<? super T> formatter = getFormatter(throwable);
            formatter.write(pw, throwable);

            for (int i = 0; i <= m; i++) {
                pw.println("\tat " + trace[i]);
            }
            if (framesInCommon != 0) {
                pw.println("\t... " + framesInCommon + " more");
            }
            // Recurse if we have a cause
            Throwable ourCause = throwable.getCause();
            if (ourCause != null) {
                printStackTraceAsCause(pw, ourCause, trace);
            }
        }
    }
}
