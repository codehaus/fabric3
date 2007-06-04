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

import java.net.URI;
import java.net.URL;
import java.util.Set;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.resource.ResourceContainerBuilder;
import org.fabric3.spi.builder.resource.ResourceContainerBuilderRegistry;
import org.fabric3.spi.deployer.CompositeClassLoader;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;

/**
 * ResourceContainerBuilder implementation that creates multi-parent classloaders from {@link
 * PhysicalClassLoaderDefinition}s. If a classloader with the same id already exists, a new one will not be created.
 * Currently, this builder creates classloaders under the boot classloader hierarchy.
 * <p/>
 * TODO replace classLoaderRegistry with ResourceManager
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ClassLoaderBuilder implements ResourceContainerBuilder<PhysicalClassLoaderDefinition> {
    public static final URI APPLICATION_CLASSLOADER = URI.create("sca://./applicationClassLoader");

    private ResourceContainerBuilderRegistry builderRegistry;
    private ClassLoaderRegistry classLoaderRegistry;
    private URI domainUri;

    public ClassLoaderBuilder(@Reference ResourceContainerBuilderRegistry builderRegistry,
                              @Reference ClassLoaderRegistry classLoaderRegistry,
                              @Reference HostInfo info) {
        this.builderRegistry = builderRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        domainUri = info.getDomain();
    }

    @Init
    public void init() {
        builderRegistry.register(PhysicalClassLoaderDefinition.class, this);
    }

    public void build(PhysicalClassLoaderDefinition definition) throws BuilderException {
        URI name = definition.getUri();
        if (classLoaderRegistry.getClassLoader(name) != null) {
            // classloader is already provisioned
            return;
        }
        Set<URL> urls = definition.getUrls();
        URL[] classpath = urls.toArray(new URL[urls.size()]);
        CompositeClassLoader loader = new CompositeClassLoader(name, classpath, null);
        for (URI uri : definition.getParentClassLoaders()) {
            ClassLoader parent = classLoaderRegistry.getClassLoader(uri);
            if (parent == null) {
                if (domainUri.equals(uri)) {
                    // get the top-level app classloader
                    parent = classLoaderRegistry.getClassLoader(APPLICATION_CLASSLOADER);
                }
                if (parent == null) {
                    throw new ClassLoaderNotFoundException("Parent classloader not found", uri.toString());
                }
            }
            loader.addParent(parent);
        }
        classLoaderRegistry.register(name, loader);
    }


}
