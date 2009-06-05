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
package org.fabric3.system.runtime;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectableAttributeType;
import org.fabric3.pojo.builder.PojoSourceWireAttacher;
import org.fabric3.pojo.builder.ProxyService;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;
import org.fabric3.system.provision.SystemWireSourceDefinition;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class SystemSourceWireAttacher extends PojoSourceWireAttacher implements SourceWireAttacher<SystemWireSourceDefinition> {

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

    public void attachToSource(SystemWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
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

    public void detachFromSource(SystemWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        detachObjectFactory(source, target);
    }

    public void detachObjectFactory(SystemWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        SystemComponent<?> component = (SystemComponent) manager.getComponent(sourceName);
        InjectableAttribute injectableAttribute = source.getValueSource();
        component.removeObjectFactory(injectableAttribute);
    }

    public void attachObjectFactory(SystemWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target)
            throws WiringException {
        URI sourceId = UriHelper.getDefragmentedName(source.getUri());
        SystemComponent<?> sourceComponent = (SystemComponent<?>) manager.getComponent(sourceId);
        InjectableAttribute referenceSource = source.getValueSource();
        Object key = getKey(source, sourceComponent, target, referenceSource);
        sourceComponent.setObjectFactory(referenceSource, objectFactory, key);
    }
}