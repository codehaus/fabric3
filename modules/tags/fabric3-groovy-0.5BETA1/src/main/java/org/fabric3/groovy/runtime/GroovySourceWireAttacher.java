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
package org.fabric3.groovy.runtime;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.groovy.provision.GroovyWireSourceDefinition;
import org.fabric3.pojo.wire.PojoSourceWireAttacher;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.proxy.ProxyService;
import org.fabric3.transform.PullTransformer;
import org.fabric3.transform.TransformerRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;

/**
 * The component builder for Java implementation types. Responsible for creating the Component runtime artifact from a physical component definition
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class GroovySourceWireAttacher extends PojoSourceWireAttacher implements SourceWireAttacher<GroovyWireSourceDefinition> {
    private final ComponentManager manager;
    private final ProxyService proxyService;
    private final ClassLoaderRegistry classLoaderRegistry;

    public GroovySourceWireAttacher(@Reference ComponentManager manager,
                                    @Reference ProxyService proxyService,
                                    @Reference ClassLoaderRegistry classLoaderRegistry,
                                    @Reference(name = "transformerRegistry")TransformerRegistry<PullTransformer<?, ?>> transformerRegistry) {
        super(transformerRegistry, classLoaderRegistry);
        this.manager = manager;
        this.proxyService = proxyService;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attachToSource(GroovyWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) throws WireAttachException {
        URI sourceUri = sourceDefinition.getUri();
        URI sourceName = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        GroovyComponent<?> source = (GroovyComponent) manager.getComponent(sourceName);
        InjectableAttribute injectableAttribute = sourceDefinition.getValueSource();

        Class<?> type;
        try {
            type = classLoaderRegistry.loadClass(sourceDefinition.getClassLoaderId(), sourceDefinition.getInterfaceName());
        } catch (ClassNotFoundException e) {
            String name = sourceDefinition.getInterfaceName();
            throw new WireAttachException("Unable to load interface class [" + name + "]", sourceUri, null, e);
        }
        if (InjectableAttributeType.CALLBACK.equals(injectableAttribute.getValueType())) {
            URI targetUri = targetDefinition.getUri();
            ScopeContainer<?> container = source.getScopeContainer();
            ObjectFactory<?> factory = proxyService.createCallbackObjectFactory(type, container, targetUri, wire);
            // JFM TODO inject updates to object factory as this does not support a proxy fronting multiple callback wires
            source.setObjectFactory(injectableAttribute, factory);
        } else {
            String callbackUri = null;
            URI uri = targetDefinition.getCallbackUri();
            if (uri != null) {
                callbackUri = uri.toString();
            }
            ObjectFactory<?> factory = proxyService.createObjectFactory(type, sourceDefinition.getInteractionType(), wire, callbackUri);
            Object key = getKey(sourceDefinition, source, injectableAttribute);
            source.attachReferenceToTarget(injectableAttribute, factory, key);
        }
    }

    public void detachFromSource(GroovyWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        throw new AssertionError();
    }

    public void attachObjectFactory(GroovyWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        URI sourceId = UriHelper.getDefragmentedName(source.getUri());
        GroovyComponent<?> sourceComponent = (GroovyComponent<?>) manager.getComponent(sourceId);
        InjectableAttribute attribute = source.getValueSource();

        Object key = getKey(source, sourceComponent, attribute);
        sourceComponent.attachReferenceToTarget(attribute, objectFactory, key);
    }
}
