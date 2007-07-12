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
import java.util.jar.JarFile;

import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.Bootstrapper;
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

    private BootstrapHelper() {
    }

    /**
     * Gets the installation directory based on the location of a class file. If the system property
     * <code>fabric3.installDir</code> is set then its value is used as the location of the installation directory.
     * Otherwise, we assume we are running from an executable jar containing the supplied class and the installation
     * directory is assumed to be the parent of the directory containing that jar.
     *
     * @param clazz the class to use as a way to find the executable jar
     * @return directory where Fabric3 standalone server is installed.
     * @throws IllegalArgumentException if the property is set but its value is not an existing directory
     * @throws IllegalStateException    if the location could not be determined from the location of the class file
     */
    public static File getInstallDirectory(Class clazz) throws IllegalStateException, IllegalArgumentException {

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
     * Get the directory associated with a runtime profile. If the system property <code>fabric3.profileDir.${profileName}</code>
     * is set then its value is used as the value for the profile directory. Otherwise, the directory
     * ${installDir}/profiles/${profileName} is used.
     *
     * @param installDir  the installation directory
     * @param profileName tha name of the profile
     * @return the directory for the the specified profile
     * @throws FileNotFoundException if the directory does not exist
     */
    public static File getProfileDirectory(File installDir, String profileName) throws FileNotFoundException {
        String propName = "fabric3.profileDir." + profileName;
        String profilePath = System.getProperty(propName);
        File profileDir;
        if (profilePath != null) {
            profileDir = new File(profilePath);
        } else {
            profileDir = new File(new File(installDir, "profiles"), profileName);
        }

        if (!profileDir.isDirectory()) {
            throw new FileNotFoundException("Unable to locate profile directory: " + profileDir.toString());
        }
        return profileDir;
    }

    /**
     * Gets the boot directory where all the boot libraries are stored. This is expected to be a directory named
     * <code>boot</code> under the install directory.
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
     * Gets the boot directory for the specified profile. If the bootPath is not null then it is used to specify the
     * location of the boot directory relative to the profile directory. Otherwise, if there is a directory named "boot"
     * relative to the profile or install directory then it is used.
     *
     * @param installDir the installation directory
     * @param profileDir the profile directory
     * @param bootPath   the path to the boot directory
     * @return the boot directory
     * @throws FileNotFoundException if the boot directory does not exist
     */
    public static File getBootDirectory(File installDir, File profileDir, String bootPath)
            throws FileNotFoundException {
        File bootDir;
        if (bootPath != null) {
            bootDir = new File(profileDir, bootPath);
        } else {
            bootDir = new File(profileDir, "boot");
            if (!bootDir.isDirectory()) {
                bootDir = new File(installDir, "boot");
            }
        }
        if (!bootDir.isDirectory()) {
            throw new FileNotFoundException("Unable to locate boot directory: " + bootDir);
        }
        return bootDir;
    }

    /**
     * Create a classloader from all the jar files or subdirectories in a directory. The classpath for the returned
     * classloader will comprise all jar files and subdirectories of the supplied directory. Hidden files and those that
     * do not contain a valid manifest will be silently ignored.
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

    /**
     * Convert a File to a URL. Equivalent to file.toURI().toURL()
     *
     * @param file the file to convert
     * @return the URL for the File
     */
    public static URL toURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            // toURI should have escaped this
            throw new AssertionError();
        }
    }

    public static StandaloneHostInfo createHostInfo(File installDir, String profile) throws IOException {
        File profileDir = getProfileDirectory(installDir, profile);

        // load properties for this runtime
        File propFile = new File(profileDir, "etc/runtime.properties");
        Properties props = loadProperties(propFile, System.getProperties());

        // online unless the offline property is set
        boolean online = !Boolean.parseBoolean(props.getProperty("offline", "false"));

        // create the classloader for booting the runtime
        String bootPath = props.getProperty("fabric3.bootDir", null);
        File bootDir = getBootDirectory(installDir, profileDir, bootPath);
        ClassLoader hostClassLoader = ClassLoader.getSystemClassLoader();
        ClassLoader bootClassLoader = createClassLoader(hostClassLoader, bootDir);

        try {

            // set the domain from runtime properties
            String domainName = props.getProperty("domain");
            URI domain = null;
            if (domainName != null) {
                domain = new URI(domainName);
            }

            return new StandaloneHostInfoImpl(domain,
                                              profile,
                                              installDir,
                                              profileDir,
                                              null,
                                              online,
                                              props,
                                              hostClassLoader,
                                              bootClassLoader);
        } catch (URISyntaxException ex) {
            throw new IOException(ex.getMessage());
        }

    }

    public static StandaloneRuntime createRuntime(StandaloneHostInfo hostInfo) throws Exception {
        ClassLoader hostClassLoader = ClassLoader.getSystemClassLoader();

        // locate the implementation
        String className = hostInfo.getProperty("fabric3.runtimeClass",
                                                "org.fabric3.runtime.standalone.host.StandaloneRuntimeImpl");
        Class<?> implClass = Class.forName(className, true, hostInfo.getBootClassLoader());

        StandaloneRuntime runtime = (StandaloneRuntime) implClass.newInstance();
        runtime.setHostClassLoader(hostClassLoader);
        runtime.setHostInfo(hostInfo);
        return runtime;
    }

    public static Bootstrapper createBootstrapper(StandaloneHostInfo hostInfo) throws Exception {
        // locate the system SCDL
        File profileDir = hostInfo.getProfileDirectory();
        URL profileURL = toURL(profileDir);
        URL systemSCDL = new URL(profileURL, hostInfo.getProperty("fabric3.systemSCDL", "system.composite"));

        // locate the implementation
        String className = hostInfo.getProperty("fabric3.bootstrapperClass",
                                                "org.fabric3.fabric.runtime.ScdlBootstrapperImpl");
        Class<?> implClass = Class.forName(className, true, hostInfo.getBootClassLoader());
        ScdlBootstrapper bootstrapper = (ScdlBootstrapper) implClass.newInstance();
        bootstrapper.setScdlLocation(systemSCDL);
        return bootstrapper;
    }

    @SuppressWarnings({"unchecked"})
    public static RuntimeLifecycleCoordinator<StandaloneRuntime, Bootstrapper> createCoordinator(StandaloneHostInfo hostInfo)
            throws Exception {
        String className = hostInfo.getProperty("fabric3.coordinatorClass",
                                                "org.fabric3.runtime.standalone.host.StandaloneCoordinator");
        Class<?> implClass = Class.forName(className, true, hostInfo.getBootClassLoader());
        return (RuntimeLifecycleCoordinator<StandaloneRuntime, Bootstrapper>) implClass.newInstance();
    }
}
