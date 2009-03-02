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
package org.fabric3.spi.classloader;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import sun.security.util.SecurityConstants;

/**
 * A classloader implementation that supports a multi-parent hierarchy and extension resolution mechanism. Class resolution is performed in the
 * following order:
 * <pre>
 * <ul>
 *   <li>Parents are searched. Parents will delegate to their classloader hierarchy.
 *   <li>If a resource is not found, the current classloader is searched.
 *   <li>If a resource is not found, extension classloaders are searched. Extension classloaders will not delegate to their classloader hierarchy.
 * </ul>
 * </pre>
 * The extension mechanism allows classes to be dyamically loaded via Class.forName() and ClassLoader.loadClass(). This is used to accomodate
 * contributions and libraries that rely on Java reflection to add additional capabilities provided by another contribution. Since reslution is
 * performed dynamically, cycles between classloaders are supported where one classloader is a parent of the other and the former is an extension of
 * the latter.
 * <p/>
 * Each classloader has a name that can be used to reference it in the runtime.
 *
 * @version $Rev$ $Date$
 */
public class MultiParentClassLoader extends URLClassLoader {
    private static final URL[] NOURLS = {};

    private final URI name;
    private final List<ClassLoader> parents = new CopyOnWriteArrayList<ClassLoader>();
    private final List<MultiParentClassLoader> extensions = new CopyOnWriteArrayList<MultiParentClassLoader>();

    /**
     * Constructs a classloader with a name and a single parent.
     *
     * @param name   a name used to identify this classloader
     * @param parent the initial parent
     */
    public MultiParentClassLoader(URI name, ClassLoader parent) {
        this(name, NOURLS, parent);
    }

    /**
     * Constructs a classloader with a name, a set of resources and a single parent.
     *
     * @param name   a name used to identify this classloader
     * @param urls   the URLs from which to load classes and resources
     * @param parent the initial parent
     */
    public MultiParentClassLoader(URI name, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.name = name;
    }


    /**
     * Add a resource URL to this classloader's classpath. The "createClassLoader" RuntimePermission is required.
     *
     * @param url an additional URL from which to load classes and resources
     */
    public void addURL(URL url) {
        // Require RuntimePermission("createClassLoader")
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkCreateClassLoader();
        }
        super.addURL(url);
    }

    /**
     * Add a parent to this classloader. The "createClassLoader" RuntimePermission is required.
     *
     * @param parent an additional parent classloader
     */
    public void addParent(ClassLoader parent) {
        // Require RuntimePermission("createClassLoader")
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkCreateClassLoader();
        }
        if (parent != null) {
            parents.add(parent);
        }
    }

    /**
     * Returns the name of this classloader.
     *
     * @return the name of this classloader
     */
    public URI getName() {
        return name;
    }

    /**
     * Returns the parent classLoaders. The "getClassLoader" RuntimePermission is required.
     *
     * @return the parent classLoaders
     */
    public List<ClassLoader> getParents() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
        }
        List<ClassLoader> list = new ArrayList<ClassLoader>();
        if (getParent() != null) {
            list.add(getParent());
        }
        list.addAll(parents);
        return list;
    }

    /**
     * Adds a classloader as an extension of this classloader.
     *
     * @param classloader the extension classloader.
     */
    public void addExtensionClassLoader(MultiParentClassLoader classloader) {
        extensions.add(classloader);
    }

    /**
     * Resolves a resource only in this classloader. Note this method does not delegate to parent classloaders.
     *
     * @param name the resource name
     * @return the resource URL or null if not found
     */
    public URL findExtensionResource(String name) {
        // look in our classpath
        return super.findResource(name);
    }

    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // look for already loaded classes
        Class<?> clazz = findLoadedClass(name);
        if (clazz == null) {
            try {
                // look in the primary parent
                try {
                    clazz = Class.forName(name, resolve, getParent());
                } catch (ClassNotFoundException e) {
                    // continue
                }
                if (clazz == null) {
                    // look in our parents
                    for (ClassLoader parent : parents) {
                        try {
                            clazz = parent.loadClass(name);
                            break;
                        } catch (ClassNotFoundException e) {
                            continue;
                        }
                    }
                }
                // look in our classpath
                if (clazz == null) {
                    try {
                        clazz = findClass(name);
                    } catch (ClassNotFoundException e) {
                        // look in extensions
                        for (MultiParentClassLoader extension : extensions) {
                            // check first to see if class is already loaded
                            clazz = extension.findLoadedClass(name);
                            if (clazz == null) {
                                clazz = extension.findClass(name);
                            }
                            if (clazz != null) {
                                break;
                            }
                        }
                        if (clazz == null) {
                            throw e;
                        }
                    }
                }
            } catch (NoClassDefFoundError e) {
                throw e;
            } catch (ClassNotFoundException e) {
                throw e;
            }
        }
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    protected Class<?> findClass(String string) throws ClassNotFoundException {
        return super.findClass(string);
    }

    public URL findResource(String name) {
        // look in our parents
        for (ClassLoader parent : parents) {
            URL resource = parent.getResource(name);
            if (resource != null) {
                return resource;
            }
        }
        // look in our classpath
        URL resource = super.findResource(name);
        if (resource == null) {
            // look in extensions
            for (MultiParentClassLoader extension : extensions) {
                resource = extension.findExtensionResource(name);
                if (resource != null) {
                    return resource;
                }
            }
        }
        return resource;
    }

    public Enumeration<URL> findResources(String name) throws IOException {
        // LinkedHashSet because we want all resources in the order found but no duplicates
        Set<URL> resources = new LinkedHashSet<URL>();
        for (ClassLoader parent : parents) {
            Enumeration<URL> parentResources = parent.getResources(name);
            while (parentResources.hasMoreElements()) {
                resources.add(parentResources.nextElement());
            }
        }
        Enumeration<URL> myResources = super.findResources(name);
        while (myResources.hasMoreElements()) {
            resources.add(myResources.nextElement());
        }
        return Collections.enumeration(resources);
    }


    public String toString() {
        return name.toString();
    }
}
