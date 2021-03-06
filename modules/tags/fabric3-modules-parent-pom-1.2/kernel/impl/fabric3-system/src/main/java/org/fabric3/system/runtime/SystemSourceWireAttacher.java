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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.system.runtime;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectableAttributeType;
import org.fabric3.pojo.builder.PojoSourceWireAttacher;
import org.fabric3.pojo.builder.ProxyService;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;
import org.fabric3.system.provision.SystemSourceDefinition;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class SystemSourceWireAttacher extends PojoSourceWireAttacher implements SourceWireAttacher<SystemSourceDefinition> {

    private final ComponentManager manager;
    private ProxyService proxyService;

    public SystemSourceWireAttacher(@Reference ComponentManager manager,
                                    @Reference(name = "transformerRegistry") TransformerRegistry<PullTransformer<?, ?>> transformerRegistry,
                                    @Reference ClassLoaderRegistry classLoaderRegistry) {
        super(transformerRegistry, classLoaderRegistry);
        this.manager = manager;
    }

    /**
     * Used for lazy injection of the proxy service. Since the ProxyService is only available after extensions are loaded and this class is loaded
     * during runtime boostrap, injection of the former service must be delayed. This is achieved by setting the reference to no required. when the
     * ProxyService becomes available, it will be wired to this reference.
     *
     * @param proxyService the service used to create reference proxies
     */
    @Reference(required = false)
    public void setProxyService(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    public void attach(SystemSourceDefinition source, PhysicalTargetDefinition target, Wire wire) throws WiringException {
        if (proxyService == null) {
            throw new WiringException("Attempt to inject a non-optimized wire during runtime boostrap.");
        }
        URI sourceUri = source.getUri();
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        SystemComponent<?> component = (SystemComponent) manager.getComponent(sourceName);
        InjectableAttribute injectableAttribute = source.getValueSource();

        Class<?> type;
        try {
            type = classLoaderRegistry.loadClass(source.getClassLoaderId(), source.getInterfaceName());
        } catch (ClassNotFoundException e) {
            String name = source.getInterfaceName();
            throw new WireAttachException("Unable to load interface class: " + name, sourceUri, null, e);
        }
        if (InjectableAttributeType.CALLBACK.equals(injectableAttribute.getValueType())) {
            throw new UnsupportedOperationException("Callbacks are not supported on system components");
        } else {
            String callbackUri = null;
            URI uri = target.getCallbackUri();
            if (uri != null) {
                callbackUri = uri.toString();
            }

            ObjectFactory<?> factory = proxyService.createObjectFactory(type, source.getInteractionType(), wire, callbackUri);
            Object key = getKey(source, component, target, injectableAttribute);
            component.setObjectFactory(injectableAttribute, factory, key);
        }
    }

    public void detach(SystemSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        detachObjectFactory(source, target);
    }

    public void detachObjectFactory(SystemSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        SystemComponent<?> component = (SystemComponent) manager.getComponent(sourceName);
        InjectableAttribute injectableAttribute = source.getValueSource();
        component.removeObjectFactory(injectableAttribute);
    }

    public void attachObjectFactory(SystemSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalTargetDefinition target)
            throws WiringException {
        URI sourceId = UriHelper.getDefragmentedName(source.getUri());
        SystemComponent<?> sourceComponent = (SystemComponent<?>) manager.getComponent(sourceId);
        InjectableAttribute referenceSource = source.getValueSource();
        Object key = getKey(source, sourceComponent, target, referenceSource);
        sourceComponent.setObjectFactory(referenceSource, objectFactory, key);
    }
}