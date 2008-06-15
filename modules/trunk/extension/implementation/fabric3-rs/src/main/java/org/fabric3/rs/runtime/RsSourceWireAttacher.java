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
package org.fabric3.rs.runtime;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import org.fabric3.api.annotation.Monitor;
import org.fabric3.java.runtime.JavaComponent;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.rs.provision.RsWireSourceDefinition;
import org.fabric3.rs.runtime.rs.RsWebApplication;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.transform.PullTransformer;
import org.fabric3.transform.TransformerRegistry;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class RsSourceWireAttacher implements SourceWireAttacher<RsWireSourceDefinition> {

    private final ComponentManager manager;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final ServletHost servletHost;
    private final RsBindingWireAttacherMonitor monitor;
    private final ConcurrentHashMap<URI, RsWebApplication> webApplications;

    public RsSourceWireAttacher(@Reference ComponentManager manager,
            @Reference ServletHost servletHost,
            @Monitor RsBindingWireAttacherMonitor monitor,
            @Reference ClassLoaderRegistry classLoaderRegistry,
            @Reference(name = "transformerRegistry") TransformerRegistry<PullTransformer<?, ?>> transformerRegistry) {
        //super(transformerRegistry, classLoaderRegistry);
        this.manager = manager;
        this.servletHost = servletHost;
        this.monitor = monitor;
        this.classLoaderRegistry = classLoaderRegistry;
        this.webApplications = new ConcurrentHashMap<URI, RsWebApplication>();
    }

    public void attachToSource(RsWireSourceDefinition sourceDefinition,
            PhysicalWireTargetDefinition targetDefinition,
            Wire wire) throws WireAttachException {
        URI sourceUri = sourceDefinition.getUri();
        URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());
        //attach directly to the component itself
        JavaComponent<?> source = (JavaComponent) manager.getComponent(targetName);
        if (source == null) {
            throw new WireAttachException("Unable to obtain Component ", targetName, null, null);
        }

        Class<?> type;
        try {
            type = classLoaderRegistry.loadClass(sourceDefinition.getClassLoaderId(), sourceDefinition.getInterfaceName());
        } catch (ClassNotFoundException e) {
            String name = sourceDefinition.getInterfaceName();
            throw new WireAttachException("Unable to load interface class [" + name + "]", sourceUri, null, e);
        }

        ObjectFactory<?> factory = source.createObjectFactory();
        if (source == null) {
            throw new WireAttachException("Unable to obtain Object Factory ", targetName, null, null);
        }

        RsWebApplication application = webApplications.get(sourceUri);
        if (application == null) {
            application = new RsWebApplication(getClass().getClassLoader());
            webApplications.put(sourceUri, application);
            String servletMapping = sourceUri.getPath();
            if (!servletMapping.endsWith("/*")) {
                servletMapping = servletMapping + "/*";
            }
            servletHost.registerMapping(servletMapping, application);
        }

        if (sourceDefinition.isResource()) {
            application.addResourceFactory(type, factory);
        }

        if (sourceDefinition.isProvider()) {
            application.addProviderFactory(type, factory);
        }

        monitor.provisionedEndpoint(sourceDefinition.getInterfaceName(), (sourceDefinition.isResource() ? "resource" : " ") + (sourceDefinition.isProvider() ? "provider" : " "), sourceUri);



    }

    public void detachFromSource(RsWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        throw new AssertionError();
    }

    public void attachObjectFactory(RsWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }
}
