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
package org.fabric3.fabric.builder.classloader;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.runtime.ComponentNames;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.ContributionUriResolver;
import org.fabric3.spi.services.contribution.ResolutionException;

/**
 * Default implementation of ClassLoaderBuilder.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ClassLoaderBuilderImpl implements ClassLoaderBuilder {

    private ClassLoaderRegistry classLoaderRegistry;
    private ContributionUriResolver contributionUriResolver;
    private ClasspathProcessorRegistry classpathProcessorRegistry;
    private boolean classLoaderIsolation;

    public ClassLoaderBuilderImpl(@Reference ClassLoaderRegistry classLoaderRegistry,
                                  @Reference ContributionUriResolver contributionUriResolver,
                                  @Reference ClasspathProcessorRegistry classpathProcessorRegistry,
                                  @Reference HostInfo info) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.contributionUriResolver = contributionUriResolver;
        this.classpathProcessorRegistry = classpathProcessorRegistry;
        classLoaderIsolation = info.supportsClassLoaderIsolation();
    }

    /**
     * Optionally, lazily injects the contribution URI resolver that may be supplied as an extension.
     *
     * @param contributionUriResolver the resolver
     */
    @Reference(required = false)
    public void setContributionUriResolver(ContributionUriResolver contributionUriResolver) {
        this.contributionUriResolver = contributionUriResolver;
    }

    public void build(PhysicalClassLoaderDefinition definition) throws ClassLoaderBuilderException {

        if (classLoaderRegistry.getClassLoader(definition.getUri()) != null) {
            updateClassLoader(definition);
        } else {
            createClassLoader(definition);
        }

    }

    public void destroy(URI uri) {
        ClassLoader loader = classLoaderRegistry.getClassLoader(uri);
        assert loader != null;
        boolean referenced = false;
        for (ClassLoader cl : classLoaderRegistry.getClassLoaders().values()) {
            if (cl == loader) {
                continue;
            }
            if (cl instanceof MultiParentClassLoader) {
                MultiParentClassLoader multi = (MultiParentClassLoader) cl;
                for (ClassLoader parent : multi.getParents()) {
                    referenced = checkReferenced(loader, parent);
                }
            } else {
                referenced = checkReferenced(loader, cl);
            }
            if (referenced) {
                break;
            }
        }
        if (!referenced) {
            classLoaderRegistry.unregister(uri);
        }
    }

    /**
     * Returns true if the first classloader is referenced in the second's classloader hierarchy.
     *
     * @param classLoaderToCheck the classloader to check
     * @param classLoader        the classloader hierarchy
     * @return true if the first classloader is referenced
     */
    private boolean checkReferenced(ClassLoader classLoaderToCheck, ClassLoader classLoader) {
        boolean referenced = false;
        while (classLoader.getParent() != null) {
            classLoader = classLoader.getParent();
            if (classLoader == classLoaderToCheck) {
                referenced = true;
                break;
            }
        }
        return referenced;
    }

    /**
     * Creates a new classloader from a PhysicalClassLoaderDefinition.
     *
     * @param definition the PhysicalClassLoaderDefinition to create the classloader from
     * @throws ClassLoaderBuilderException if an error occurs creating the classloader
     */
    private void createClassLoader(PhysicalClassLoaderDefinition definition) throws ClassLoaderBuilderException {

        URI name = definition.getUri();
        URL[] classpath = resolveClasspath(definition.getContributionUris());

        // build the classloader using the locally cached resources
        MultiParentClassLoader loader = new MultiParentClassLoader(name, classpath, null);
        // add the host classloader
        ClassLoader cl = classLoaderRegistry.getClassLoader(ComponentNames.APPLICATION_CLASSLOADER_ID);
        loader.addParent(cl);
        if (classLoaderIsolation) {
            // if the host supports isolated classloaders, add any parents
            for (URI uri : definition.getParentClassLoaders()) {
                ClassLoader parent = classLoaderRegistry.getClassLoader(uri);
                if (parent == null) {
                    String identifier = uri.toString();
                    throw new ClassLoaderNotFoundException("Parent classloader not found: " + identifier);
                }
                loader.addParent(parent);
            }
        }
        classLoaderRegistry.register(name, loader);
    }

    /**
     * Updates the given classloader with additional artifacts from the PhysicalClassLoaderDefinition. Classloader updates are typically performed
     * during an include operation where the included component requires additional libraries or classes not currently on the composite classpath.
     *
     * @param definition the definition to update the classloader with
     * @throws ClassLoaderBuilderException if an error occurs updating the classloader
     */
    private void updateClassLoader(PhysicalClassLoaderDefinition definition) throws ClassLoaderBuilderException {

        ClassLoader cl = classLoaderRegistry.getClassLoader(definition.getUri());
        assert cl instanceof MultiParentClassLoader;
        MultiParentClassLoader loader = (MultiParentClassLoader) cl;
        Set<URI> uris = definition.getContributionUris();

        for (URI uri : uris) {
            try {
                // resolve the remote artifact URL and add it to the classloader
                URL resolvedUrl = contributionUriResolver.resolve(uri);
                // introspect and expand if necessary
                List<URL> processedUrls = classpathProcessorRegistry.process(resolvedUrl);
                URL[] loaderUrls = loader.getURLs();
                // check if URLs are already on the classpath, and if so do not add them
                for (URL processedUrl : processedUrls) {
                    boolean found = false;
                    for (URL loaderUrl : loaderUrls) {
                        if (loaderUrl.equals(processedUrl)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // the URL is not on the classpath, update the classloader
                        loader.addURL(processedUrl);
                    }

                }
            } catch (ResolutionException e) {
                throw new ClassLoaderBuilderException("Error resolving artifact: " + uri.toString(), e);
            } catch (IOException e) {
                throw new ClassLoaderBuilderException("Error processing: " + uri.toString(), e);
            }
        }

        if (!definition.getExtensionUris().isEmpty()) {
            // since all extensions are merged into the system classloader, just add it
            ClassLoader systemCL = classLoaderRegistry.getClassLoader(URI.create("fabric3://runtime"));
            if (!loader.getParents().contains(systemCL)) {
                loader.addParent(systemCL);
            }
        }
        for (URI uri : definition.getParentClassLoaders()) {
            ClassLoader parent = classLoaderRegistry.getClassLoader(uri);
            if (!loader.getParents().contains(parent)) {
                loader.addParent(parent);
            }
        }

    }

    /**
     * Resolves classpath urls
     *
     * @param uris urls to resolve
     * @return the resolved classpath urls
     * @throws ClassLoaderBuilderException if an error occurs resolving a url
     */
    private URL[] resolveClasspath(Set<URI> uris) throws ClassLoaderBuilderException {

        List<URL> classpath = new ArrayList<URL>();

        for (URI uri : uris) {
            try {
                // resolve the remote contributions and cache them locally
                URL resolvedUrl = contributionUriResolver.resolve(uri);
                // introspect and expand if necessary
                classpath.addAll(classpathProcessorRegistry.process(resolvedUrl));
            } catch (ResolutionException e) {
                throw new ClassLoaderBuilderException("Error resolving artifact: " + uri.toString(), e);
            } catch (IOException e) {
                throw new ClassLoaderBuilderException("Error processing: " + uri.toString(), e);
            }
        }
        return classpath.toArray(new URL[classpath.size()]);

    }

}
