/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.fabric.classloader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.runtime.ComponentNames;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.builder.resource.ResourceContainerBuilder;
import org.fabric3.spi.builder.resource.ResourceContainerBuilderRegistry;
import org.fabric3.spi.deployer.CompositeClassLoader;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.ArtifactResolverRegistry;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.ResolutionException;

/**
 * ResourceContainerBuilder implementation that creates multi-parent classloaders from {@link
 * PhysicalClassLoaderDefinition}s. If a classloader with the same id already exists, a new one will not be created.
 * Currently, this builder creates classloaders under the boot classloader hierarchy.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ClassLoaderBuilder implements ResourceContainerBuilder<PhysicalClassLoaderDefinition> {
    public static final URI APPLICATION_CLASSLOADER = URI.create("sca://./applicationClassLoader");
    public static final URI BOOT_CLASSLOADER = URI.create("sca://./bootClassLoader");

    private ResourceContainerBuilderRegistry builderRegistry;
    private ClassLoaderRegistry classLoaderRegistry;
    private ArtifactResolverRegistry artifactResolverRegistry;
    private ClasspathProcessorRegistry classpathProcessorRegistry;
    private URI domainUri;

    public ClassLoaderBuilder(@Reference ResourceContainerBuilderRegistry builderRegistry,
                              @Reference ClassLoaderRegistry classLoaderRegistry,
                              @Reference ArtifactResolverRegistry artifactResolverRegistry,
                              @Reference HostInfo info,
                              @Reference ClasspathProcessorRegistry classpathProcessorRegistry) {
        this.builderRegistry = builderRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.artifactResolverRegistry = artifactResolverRegistry;
        this.classpathProcessorRegistry = classpathProcessorRegistry;
        domainUri = info.getDomain();
    }

    @Init
    public void init() {
        builderRegistry.register(PhysicalClassLoaderDefinition.class, this);
    }

    public void build(PhysicalClassLoaderDefinition definition) throws ClassLoaderBuilderException {
        URI name = definition.getUri();
        ClassLoader cl = classLoaderRegistry.getClassLoader(name);
        if (cl != null) {
            assert cl instanceof URLClassLoader;
            updateClassLoader(cl, definition);
        } else {
            createClassLoader(name, definition);
        }
    }

    /**
     * Creates a new classloader from a PhysicalClassLoaderDefinition.
     *
     * @param name       the classloader name
     * @param definition the PhysicalClassLoaderDefinition to create the classloader from
     * @throws ClassLoaderBuilderException if an error occurs creating the classloader
     */
    private void createClassLoader(URI name, PhysicalClassLoaderDefinition definition)
            throws ClassLoaderBuilderException {
        URL[] classpath = resolveClasspath(definition.getResourceUrls());
        // build the classloader using the locally cached resources
        CompositeClassLoader loader = new CompositeClassLoader(name, classpath, null);
        for (URI uri : definition.getParentClassLoaders()) {
            ClassLoader parent = classLoaderRegistry.getClassLoader(uri);
            if (parent == null) {
                if (domainUri.equals(uri)) {
                    // the classloader is being created in the application compononent heierarchy.
                    parent = classLoaderRegistry.getClassLoader(APPLICATION_CLASSLOADER);
                } else if (ComponentNames.RUNTIME_URI.equals(uri)) {
                    // the classloader is being created in the system compononent heierarchy. Use the boot cl
                    parent = classLoaderRegistry.getClassLoader(BOOT_CLASSLOADER);
                }
                if (parent == null) {
                    throw new ClassLoaderNotFoundException("Parent classloader not found", uri.toString());
                }
            }
            loader.addParent(parent);
        }
        classLoaderRegistry.register(name, loader);
    }

    /**
     * Updates the given classloader with additional artifacts from the PhysicalClassLoaderDefinition. Classloader
     * updates are typically performed during an include operation where the included component requires additional
     * libraries or classes not currently on the composite classpath.
     *
     * @param cl         the classloader to update
     * @param definition the definition to update the classloader with
     * @throws ClassLoaderBuilderException if an error occurs updating the classloader
     */
    private void updateClassLoader(ClassLoader cl, PhysicalClassLoaderDefinition definition)
            throws ClassLoaderBuilderException {
        assert cl instanceof CompositeClassLoader;
        List<URL> classpath = new ArrayList<URL>();
        CompositeClassLoader loader = (CompositeClassLoader) cl;
        Set<URL> urls = definition.getResourceUrls();
        URL[] loaderUrls = loader.getURLs();
        for (URL url : urls) {
            boolean found = false;
            for (URL loaderUrl : loaderUrls) {
                if (loaderUrl.equals(url)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                try {
                    // resolve the remote artifact URL and add it to the classloader
                    URL resolvedUrl = artifactResolverRegistry.resolve(url);
                    // introspect and expand if necessary
                    File file = new File(resolvedUrl.getFile());
                    classpath.addAll(classpathProcessorRegistry.process(file));
                } catch (ResolutionException e) {
                    throw new ClassLoaderBuilderException("Error resolving artifact", e);
                } catch (IOException e) {
                    throw new ClassLoaderBuilderException("Error processing", url.toString(), e);
                }
            }
        }
        for (URL url : classpath) {
            loader.addURL(url);
        }
    }

    /**
     * Creates a child classloader from the PhysicalClassLoaderDefinition. The child classloader classpath will contain
     * artifacts specified in the definiton and not already on the parent classpath.
     *
     * @param parent     the parent classloader
     * @param definition the PhysicalClassLoaderDefinition to create the classloader from
     * @return the child classloader
     * @throws ClassLoaderBuilderException if an error occurs creating the classloader, such as resolving an artifact
     */
    private CompositeClassLoader createChildClassLoader(URLClassLoader parent, PhysicalClassLoaderDefinition definition)
            throws ClassLoaderBuilderException {
        Set<URL> urls = definition.getResourceUrls();
        URL[] loaderUrls = parent.getURLs();
        Set<URL> resolvedUrls = new HashSet<URL>();
        // resolve the urls and add ones not present in the parent to the child
        for (URL url : urls) {
            boolean found = false;
            // resolve the remote artifact URL and add it to the classloader
            try {
                URL resolvedUrl = artifactResolverRegistry.resolve(url);
                // introspect and expand if necessary
                File file = new File(resolvedUrl.getFile());
                List<URL> classpath = classpathProcessorRegistry.process(file);
                for (URL entry : classpath) {
                    for (URL loaderUrl : loaderUrls) {
                        if (loaderUrl.equals(entry)) {
                            found = true;
                            break;
                        }
                        if (!found) {
                            resolvedUrls.add(entry);
                        }
                    }
                }
            } catch (ResolutionException e) {
                throw new ClassLoaderBuilderException("Error resolving artifact", e);
            } catch (IOException e) {
                throw new ClassLoaderBuilderException("Error processing", url.toString(), e);
            }
        }
        URL[] classpath = resolvedUrls.toArray(new URL[resolvedUrls.size()]);
        return new CompositeClassLoader(definition.getUri(), classpath, parent);
    }

    /**
     * Resolves classpath urls
     *
     * @param urls urls to resolve
     * @return the resolved classpath urls
     * @throws ClassLoaderBuilderException if an error occurs resolving a url
     */
    private URL[] resolveClasspath(Set<URL> urls) throws ClassLoaderBuilderException {
        List<URL> classpath = new ArrayList<URL>();
        for (URL url : urls) {
            try {
                // resolve the remote artifact URLs and cache them locally
                URL resolvedUrl = artifactResolverRegistry.resolve(url);
                // introspect and expand if necessary
                File file = new File(resolvedUrl.getFile());
                classpath.addAll(classpathProcessorRegistry.process(file));
            } catch (ResolutionException e) {
                throw new ClassLoaderBuilderException("Error resolving artifact", e);
            } catch (IOException e) {
                throw new ClassLoaderBuilderException("Error processing", url.toString(), e);
            }
        }
        return classpath.toArray(new URL[classpath.size()]);
    }
}
