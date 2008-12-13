/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
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
package org.fabric3.runtime.standalone;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.jar.JarFile;

import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;

/**
 * Utility class for boostrap related operations.
 *
 * @version $Revision$ $Date$
 */
public final class BootstrapHelper {

    /**
     * Installation directory system property name.
     */
    private static final String INSTALL_DIRECTORY_PROPERTY = "fabric3.installDir";
    private static final String DEFAULT_MONITOR_FACTORY = "org.fabric3.monitor.impl.JavaLoggingMonitorFactory";

    private BootstrapHelper() {
    }

    /**
     * Gets the installation directory based on the location of a class file. If the system property <code>fabric3.installDir</code> is set then its
     * value is used as the location of the installation directory. Otherwise, we assume we are running from an executable jar containing the supplied
     * class and the installation directory is assumed to be the parent of the directory containing that jar.
     *
     * @param clazz the class to use as a way to find the executable jar
     * @return directory where Fabric3 standalone server is installed.
     * @throws IllegalArgumentException if the property is set but its value is not an existing directory
     * @throws IllegalStateException    if the location could not be determined from the location of the class file
     */
    public static File getInstallDirectory(Class<?> clazz) throws IllegalStateException, IllegalArgumentException {

        String installDirectoryPath = System.getProperty(INSTALL_DIRECTORY_PROPERTY);

        if (installDirectoryPath != null) {
            File installDirectory = new File(installDirectoryPath);
            if (!installDirectory.exists()) {
                throw new IllegalArgumentException(INSTALL_DIRECTORY_PROPERTY
                        + " property does not refer to an existing directory: " + installDirectory);
            }
            return installDirectory;
        }

        // get the name of the Class's bytecode
        String name = clazz.getName();
        int last = name.lastIndexOf('.');
        if (last != -1) {
            name = name.substring(last + 1);
        }
        name = name + ".class";

        // get location of the bytecode - should be a jar: URL
        URL url = clazz.getResource(name);
        if (url == null) {
            throw new IllegalStateException("Unable to get location of bytecode resource " + name);
        }

        String jarLocation = url.toString();
        if (!jarLocation.startsWith("jar:")) {
            throw new IllegalStateException("Must be run from a jar: " + url);
        }

        // extract the location of thr jar from the resource URL 
        jarLocation = jarLocation.substring(4, jarLocation.lastIndexOf("!/"));
        if (!jarLocation.startsWith("file:")) {
            throw new IllegalStateException("Must be run from a local filesystem: " + jarLocation);
        }

        File jarFile = new File(URI.create(jarLocation));
        return jarFile.getParentFile().getParentFile();
    }

    /**
     * Gets the directory for the specified base directory/path combination.
     *
     * @param baseDir the base directory
     * @param path    the  path
     * @return the boot directory
     * @throws FileNotFoundException if the boot directory does not exist
     */
    public static File getDirectory(File baseDir, String path) throws FileNotFoundException {
        File dir = new File(baseDir, path);
        if (!dir.exists()) {
            throw new FileNotFoundException("Directory does not exist: " + dir);
        }
        if (!dir.isDirectory()) {
            throw new FileNotFoundException("Resource is not a directory: " + dir);
        }
        return dir;
    }

    /**
     * Create a classloader from all the jar files or subdirectories in a directory. The classpath for the returned classloader will comprise all jar
     * files and subdirectories of the supplied directory. Hidden files and those that do not contain a valid manifest will be silently ignored.
     *
     * @param parent    the parent for the new classloader
     * @param directory the directory to scan
     * @return a classloader whose classpath includes all jar files and subdirectories of the supplied directory
     */
    public static ClassLoader createClassLoader(ClassLoader parent, File directory) {
        File[] jars = directory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.isHidden()) {
                    return false;
                }
                if (file.isDirectory()) {
                    return true;
                }
                try {
                    JarFile jar = new JarFile(file);
                    return jar.getManifest() != null;
                } catch (IOException e) {
                    return false;
                }
            }
        });

        URL[] urls = new URL[jars.length];
        for (int i = 0; i < jars.length; i++) {
            try {
                urls[i] = jars[i].toURI().toURL();
            } catch (MalformedURLException e) {
                // toURI should have escaped the URL
                throw new AssertionError();
            }
        }

        return new URLClassLoader(urls, parent);
    }

    /**
     * Load properties from the specified file. If the file does not exist then an empty properties object is returned.
     *
     * @param propFile the file to load from
     * @param defaults defaults for the properties
     * @return a Properties object loaded from the file
     * @throws IOException if there was a problem loading the properties
     */
    public static Properties loadProperties(File propFile, Properties defaults) throws IOException {
        Properties props = defaults == null ? new Properties() : new Properties(defaults);
        FileInputStream is;
        try {
            is = new FileInputStream(propFile);
        } catch (FileNotFoundException e) {
            return props;
        }
        try {
            props.load(is);
            return props;
        } finally {
            is.close();
        }
    }

    public static StandaloneHostInfo createHostInfo(File baseDir, File configDir, Properties props) throws BootstrapException, IOException {

        File extensionsDir = getDirectory(baseDir, "extensions");
        File tempDir = getDirectory(baseDir, "tmp");

        try {

            // set the domain from runtime properties
            String domainName = props.getProperty("domain");
            URI domain;
            if (domainName != null) {
                domain = new URI(domainName);
            } else {
                throw new BootstrapException("Domain URI was not set. Ensure it is set as a system property or in runtime.properties.");
            }

            return new StandaloneHostInfoImpl(domain, baseDir, extensionsDir, configDir, props, tempDir);
        } catch (URISyntaxException ex) {
            throw new IOException(ex.getMessage());
        }

    }

    public static MonitorFactory createDefaultMonitorFactory(ClassLoader classLoader) throws BootstrapException {
        return createMonitorFactory(classLoader, DEFAULT_MONITOR_FACTORY);
    }

    public static MonitorFactory createMonitorFactory(ClassLoader classLoader, String factoryClass) throws BootstrapException {
        try {
            Class<?> monitorClass = Class.forName(factoryClass, true, classLoader);
            return (MonitorFactory) monitorClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new BootstrapException(e);
        } catch (IllegalAccessException e) {
            throw new BootstrapException(e);
        } catch (InstantiationException e) {
            throw new BootstrapException(e);
        }
    }

    public static StandaloneRuntime createRuntime(StandaloneHostInfo hostInfo,
                                                  ClassLoader hostClassLoader,
                                                  ClassLoader bootClassLoader,
                                                  MonitorFactory monitorFactory)
            throws BootstrapException {

        // locate the implementation
        String className = hostInfo.getProperty("fabric3.runtimeClass", "org.fabric3.runtime.standalone.host.StandaloneRuntimeImpl");
        try {
            Class<?> implClass = Class.forName(className, true, bootClassLoader);
            Constructor<?> ctor = implClass.getConstructor(MonitorFactory.class);
            StandaloneRuntime runtime = (StandaloneRuntime) ctor.newInstance(monitorFactory);
            runtime.setHostClassLoader(hostClassLoader);
            runtime.setHostInfo(hostInfo);

            return runtime;
        } catch (IllegalAccessException e) {
            throw new BootstrapException(e);
        } catch (InstantiationException e) {
            throw new BootstrapException(e);
        } catch (ClassNotFoundException e) {
            throw new BootstrapException(e);
        } catch (NoSuchMethodException e) {
            throw new BootstrapException(e);
        } catch (InvocationTargetException e) {
            throw new BootstrapException(e);
        }
    }

    public static Bootstrapper createBootstrapper(StandaloneHostInfo hostInfo, ClassLoader bootClassLoader) throws BootstrapException {
        try {
            // locate the system SCDL
            File configDir = hostInfo.getConfigDirectory();
            URL configUrl = configDir.toURI().toURL();
            URL systemSCDL = new URL(configUrl, hostInfo.getProperty("fabric3.systemSCDL", "system.composite"));

            // locate the implementation
            String className = hostInfo.getProperty("fabric3.bootstrapperClass",
                                                    "org.fabric3.fabric.runtime.bootstrap.ScdlBootstrapperImpl");
            Class<?> implClass = Class.forName(className, true, bootClassLoader);
            ScdlBootstrapper bootstrapper = (ScdlBootstrapper) implClass.newInstance();
            bootstrapper.setScdlLocation(systemSCDL);
            // set the system configuration
            File systemConfig = new File(hostInfo.getConfigDirectory(), "systemConfig.xml");
            if (systemConfig.exists()) {
                bootstrapper.setSystemConfig(systemConfig.toURI().toURL());
            }
            return bootstrapper;
        } catch (IllegalAccessException e) {
            throw new BootstrapException(e);
        } catch (MalformedURLException e) {
            throw new BootstrapException(e);
        } catch (InstantiationException e) {
            throw new BootstrapException(e);
        } catch (ClassNotFoundException e) {
            throw new BootstrapException(e);
        }

    }

    @SuppressWarnings({"unchecked"})
    public static RuntimeLifecycleCoordinator<StandaloneRuntime, Bootstrapper> createCoordinator(StandaloneHostInfo hostInfo,
                                                                                                 ClassLoader bootClassLoader)
            throws BootstrapException {
        String className = hostInfo.getProperty("fabric3.coordinatorClass",
                                                "org.fabric3.fabric.runtime.DefaultCoordinator");
        try {
            Class<?> implClass = Class.forName(className, true, bootClassLoader);
            return (RuntimeLifecycleCoordinator<StandaloneRuntime, Bootstrapper>) implClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new BootstrapException(e);
        } catch (IllegalAccessException e) {
            throw new BootstrapException(e);
        } catch (InstantiationException e) {
            throw new BootstrapException(e);
        }
    }

}
