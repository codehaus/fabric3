/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.java.runtime;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.java.provision.JavaSourceDefinition;
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
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches wires to and from components implemented using the Java programming model.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JavaSourceWireAttacher extends PojoSourceWireAttacher implements SourceWireAttacher<JavaSourceDefinition> {

    private final ComponentManager manager;
    private final ProxyService proxyService;

    public JavaSourceWireAttacher(@Reference ComponentManager manager,
                                  @Reference ProxyService proxyService,
                                  @Reference ClassLoaderRegistry classLoaderRegistry,
                                  @Reference(name = "transformerRegistry") TransformerRegistry<PullTransformer<?, ?>> transformerRegistry) {
        super(transformerRegistry, classLoaderRegistry);
        this.manager = manager;
        this.proxyService = proxyService;
    }

    public void attachToSource(JavaSourceDefinition sourceDefinition, PhysicalTargetDefinition targetDefinition, Wire wire)
            throws WiringException {

        URI sourceUri = sourceDefinition.getUri();
        URI sourceName = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        JavaComponent<?> source = (JavaComponent) manager.getComponent(sourceName);
        InjectableAttribute injectableAttribute = sourceDefinition.getValueSource();

        Class<?> type;
        try {
            type = classLoaderRegistry.loadClass(sourceDefinition.getClassLoaderId(), sourceDefinition.getInterfaceName());
        } catch (ClassNotFoundException e) {
            String name = sourceDefinition.getInterfaceName();
            throw new WireAttachException("Unable to load interface class: " + name, sourceUri, null, e);
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

    public void detachFromSource(JavaSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        detachObjectFactory(source, target);
    }

    public void detachObjectFactory(JavaSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        JavaComponent<?> component = (JavaComponent) manager.getComponent(sourceName);
        InjectableAttribute injectableAttribute = source.getValueSource();
        component.removeObjectFactory(injectableAttribute);
    }

    public void attachObjectFactory(JavaSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalTargetDefinition target)
            throws WiringException {
        URI sourceId = UriHelper.getDefragmentedName(source.getUri());
        JavaComponent<?> sourceComponent = (JavaComponent<?>) manager.getComponent(sourceId);
        InjectableAttribute injectableAttribute = source.getValueSource();

        Object key = getKey(source, sourceComponent, target, injectableAttribute);
        sourceComponent.setObjectFactory(injectableAttribute, objectFactory, key);
    }
}
