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
package org.fabric3.runtime.standalone;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.jar.JarFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.monitor.MonitorFactory;

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
     * Gets the boot directory where all the boot libraries are stored. This is expected to be a directory named <code>boot</code> under the install
     * directory.
     *
     * @param installDirectory Fabric3 install directory.
     * @param bootPath         Boot path for the runtime.
     * @return Fabric3 boot directory.
     */
    public static File getBootDirectory(File installDirectory, String bootPath) {

        File bootDirectory = new File(installDirectory, bootPath);
        if (!bootDirectory.exists()) {
            throw new IllegalStateException("Boot directory doesn't exist: " + bootDirectory.getAbsolutePath());
        }
        return bootDirectory;

    }

    /**
     * Gets the directory for the specified profile. If the bootPath is not null then it is used to specify the location of the boot directory
     * relative to the profile directory. Otherwise, if there is a directory named "boot" relative to the profile or install directory then it is
     * used.
     *
     * @param installDir  the installation directory
     * @param path        the path to the boot directory
     * @param defaultPath the default path
     * @return the boot directory
     * @throws FileNotFoundException if the boot directory does not exist
     */
    public static File getDirectory(File installDir, String path, String defaultPath) throws FileNotFoundException {
        File dir;
        if (path != null) {
            dir = new File(path);
        } else {
            dir = new File(installDir, defaultPath);
        }
        if (!dir.isDirectory()) {
            throw new FileNotFoundException("Unable to locate boot directory: " + dir);
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

        // online unless the offline property is set
        boolean online = !Boolean.parseBoolean(props.getProperty("offline", "false"));
        String extensionsPath = props.getProperty("fabric3.extensionsDir", null);
        File extensionsDir = getDirectory(baseDir, extensionsPath, "extensions");

        try {

            // set the domain from runtime properties
            String domainName = props.getProperty("domain");
            URI domain;
            if (domainName != null) {
                domain = new URI(domainName);
            } else {
                throw new BootstrapException("Domain URI was not set. Ensure it is set as a system property or in runtime.properties.");
            }

            return new StandaloneHostInfoImpl(domain, baseDir, extensionsDir, configDir, online, props);
        } catch (URISyntaxException ex) {
            throw new IOException(ex.getMessage());
        }

    }

    public static MonitorFactory createMonitorFactory(ClassLoader bootClassLoader, Properties properties) throws BootstrapException {
        try {
            String monitorFactoryName = properties.getProperty("fabric3.monitorFactoryClass",
                                                               "org.fabric3.monitor.impl.JavaLoggingMonitorFactory");
            String bundleName = properties.getProperty("fabric3.monitorBundle", "f3");
            Level level = Level.parse(properties.getProperty("fabric3.defaultLevel", "FINE"));

            String formatterClass = properties.getProperty("fabric3.jdkLogFormatter", "org.fabric3.monitor.impl.Fabric3LogFormatter");
            Properties configuration = new Properties();
            configuration.setProperty("fabric3.jdkLogFormatter", formatterClass);
            Class<?> monitorClass = Class.forName(monitorFactoryName, true, bootClassLoader);
            MonitorFactory monitorFactory = (MonitorFactory) monitorClass.newInstance();
            monitorFactory.setBundleName(bundleName);
            monitorFactory.setDefaultLevel(level);
            monitorFactory.setConfiguration(configuration);
            return monitorFactory;
        } catch (ClassNotFoundException e) {
            throw new BootstrapException(e);
        } catch (IllegalAccessException e) {
            throw new BootstrapException(e);
        } catch (InstantiationException e) {
            throw new BootstrapException(e);
        }
    }

    public static StandaloneRuntime createRuntime(StandaloneHostInfo hostInfo, ClassLoader bootClassLoader, MonitorFactory monitorFactory)
            throws BootstrapException {
        ClassLoader hostClassLoader = ClassLoader.getSystemClassLoader();

        // locate the implementation
        String className = hostInfo.getProperty("fabric3.runtimeClass",
                                                "org.fabric3.runtime.standalone.host.StandaloneRuntimeImpl");
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
                                                "org.fabric3.runtime.standalone.host.StandaloneCoordinator");
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
