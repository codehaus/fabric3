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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.Names;
import static org.fabric3.host.Names.HOST_CONTRIBUTION;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.builder.classloader.ClassLoaderWireBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.contribution.ContributionUriResolver;
import org.fabric3.spi.contribution.ResolutionException;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;
import org.fabric3.spi.model.physical.PhysicalClassLoaderWireDefinition;
import org.fabric3.spi.services.componentmanager.ComponentManager;

/**
 * Default implementation of ClassLoaderBuilder.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ClassLoaderBuilderImpl implements ClassLoaderBuilder {

    private ClassLoaderWireBuilder wireBuilder;
    private ClassLoaderRegistry classLoaderRegistry;
    private ContributionUriResolver contributionUriResolver;
    private ClasspathProcessorRegistry classpathProcessorRegistry;
    private ComponentManager componentManager;
    private boolean classLoaderIsolation;

    public ClassLoaderBuilderImpl(@Reference ClassLoaderWireBuilder wireBuilder,
                                  @Reference ClassLoaderRegistry classLoaderRegistry,
                                  @Reference ContributionUriResolver contributionUriResolver,
                                  @Reference ClasspathProcessorRegistry classpathProcessorRegistry,
                                  @Reference ComponentManager componentManager,
                                  @Reference HostInfo info) {
        this.wireBuilder = wireBuilder;
        this.classLoaderRegistry = classLoaderRegistry;
        this.contributionUriResolver = contributionUriResolver;
        this.classpathProcessorRegistry = classpathProcessorRegistry;
        this.componentManager = componentManager;
        classLoaderIsolation = info.supportsClassLoaderIsolation();
    }

    /**
     * Optionally, lazily injects the contribution URI resolver that may be supplied as an extension.
     *
     * @param contributionUriResolver the resolver
     */
    @Reference
    public void setContributionUriResolver(ContributionUriResolver contributionUriResolver) {
        this.contributionUriResolver = contributionUriResolver;
    }

    public void build(PhysicalClassLoaderDefinition definition) throws ClassLoaderBuilderException {
        URI uri = definition.getUri();
        if (classLoaderRegistry.getClassLoader(uri) != null) {
            /*
             The classloader was already loaded. The classloader will already be created if: it is the boot classloader; the environment is
             single-VM as classloaders are shared between the contribution and runtime infrastructure; two composites are deployed individually
             from the same contribution.
             */
            return;
        }
        if (classLoaderIsolation) {
            buildIsolatedClassLoaderEnvironment(definition);
        } else {
            buildCommonClassLoaderEnvironment(definition);
        }
    }

    private void buildCommonClassLoaderEnvironment(PhysicalClassLoaderDefinition definition) {
        URI uri = definition.getUri();
        // Create an alias to the host classloader which contains all contribution artifacts in a non-isolated environment.
        // This simulates multiple classloaders
        ClassLoader hostClassLoader = classLoaderRegistry.getClassLoader(HOST_CONTRIBUTION);
        classLoaderRegistry.register(uri, hostClassLoader);
    }

    private void buildIsolatedClassLoaderEnvironment(PhysicalClassLoaderDefinition definition) throws ClassLoaderBuilderException {
        URI uri = definition.getUri();
        URL[] classpath = resolveClasspath(definition.getContributionUri());

        // build the classloader using the locally cached resources
        MultiParentClassLoader loader = new MultiParentClassLoader(uri, classpath, null);
        for (PhysicalClassLoaderWireDefinition wireDefinition : definition.getWireDefinitions()) {
            wireBuilder.build(loader, wireDefinition);
        }
        classLoaderRegistry.register(uri, loader);
    }

    public void destroy(URI uri) {
        ClassLoader loader = classLoaderRegistry.getClassLoader(uri);
        assert loader != null;
        List<Component> components = componentManager.getComponents();
        // remove the classloader if there are no components that reference it
        for (Component component : components) {
            if (uri.equals(component.getClassLoaderId())) {
                return;
            }
        }
        classLoaderRegistry.unregister(uri);
    }

    /**
     * Resolves classpath urls.
     *
     * @param uri uri to resolve
     * @return the resolved classpath urls
     * @throws ClassLoaderBuilderException if an error occurs resolving a url
     */
    private URL[] resolveClasspath(URI uri) throws ClassLoaderBuilderException {

        List<URL> classpath = new ArrayList<URL>();

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
        return classpath.toArray(new URL[classpath.size()]);

    }

}
