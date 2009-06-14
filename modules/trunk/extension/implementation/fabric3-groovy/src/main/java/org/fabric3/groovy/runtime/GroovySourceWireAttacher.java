  /*
   * Fabric3
   * Copyright (C) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
   */
package org.fabric3.groovy.runtime;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.groovy.provision.GroovyWireSourceDefinition;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectableAttributeType;
import org.fabric3.pojo.builder.PojoSourceWireAttacher;
import org.fabric3.pojo.builder.ProxyService;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
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
            ScopeContainer container = source.getScopeContainer();
            ObjectFactory<?> factory = source.getObjectFactory(injectableAttribute);
            if (factory == null) {
                factory = proxyService.createCallbackObjectFactory(type, container, callbackUri, wire);
            } else {
                factory = proxyService.updateCallbackObjectFactory(factory, type, container, callbackUri, wire);
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
        detachObjectFactory(source, target);
    }

    public void detachObjectFactory(GroovyWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        GroovyComponent<?> component = (GroovyComponent) manager.getComponent(sourceName);
        InjectableAttribute injectableAttribute = source.getValueSource();
        component.removeObjectFactory(injectableAttribute);
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
