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
 */
package org.fabric3.groovy.runtime;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.groovy.provision.GroovyWireSourceDefinition;
import org.fabric3.pojo.builder.PojoSourceWireAttacher;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.services.proxy.ProxyService;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;
import org.fabric3.transform.PullTransformer;
import org.fabric3.transform.TransformerRegistry;

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
                                    @Reference(name = "transformerRegistry") TransformerRegistry<PullTransformer<?, ?>> transformerRegistry) {
        super(transformerRegistry, classLoaderRegistry);
        this.manager = manager;
        this.proxyService = proxyService;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attachToSource(GroovyWireSourceDefinition sourceDefinition, PhysicalWireTargetDefinition targetDefinition, Wire wire)
            throws WiringException {
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
            URI callbackUri = targetDefinition.getUri();
            ScopeContainer<?> container = source.getScopeContainer();
            ObjectFactory<?> factory = source.getObjectFactory(injectableAttribute);
            if (factory == null) {
                factory = proxyService.createCallbackObjectFactory(type, container, callbackUri, wire);
            } else {
                factory = proxyService.updateCallbackObjectFactory(factory, callbackUri, wire);
            }
            source.setObjectFactory(injectableAttribute, factory);
        } else {
            String callbackUri = null;
            URI uri = targetDefinition.getCallbackUri();
            if (uri != null) {
                callbackUri = uri.toString();
            }
            ObjectFactory<?> factory = proxyService.createObjectFactory(type, sourceDefinition.getInteractionType(), wire, callbackUri);
            Object key = getKey(sourceDefinition, source, targetDefinition, injectableAttribute);
            source.setObjectFactory(injectableAttribute, factory, key);
        }
    }

    public void detachFromSource(GroovyWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    public void detachObjectFactory(GroovyWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void attachObjectFactory(GroovyWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition definition)
            throws WiringException {
        URI sourceId = UriHelper.getDefragmentedName(source.getUri());
        GroovyComponent<?> sourceComponent = (GroovyComponent<?>) manager.getComponent(sourceId);
        InjectableAttribute attribute = source.getValueSource();

        Object key = getKey(source, sourceComponent, definition, attribute);
        sourceComponent.setObjectFactory(attribute, objectFactory, key);
    }
}
