/*
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
package org.fabric3.fabric.implementation.java;

import java.net.URI;
import java.util.Map;

import org.osoa.sca.CallableReference;
import org.osoa.sca.ServiceReference;

import org.fabric3.fabric.component.ComponentContextProvider;
import org.fabric3.fabric.injection.MultiplicityObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.pojo.implementation.PojoComponent;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.model.instance.ValueSource;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.ObjectCreationException;

/**
 * The runtime instantiation of Java component implementations
 *
 * @version $Revision$ $Date$
 * @param <T> the implementation class for the defined component
 */
public class JavaComponent<T> extends PojoComponent<T> implements ComponentContextProvider {
    private final ProxyService proxyService;
    private final Map<String, ObjectFactory<?>> propertyFactories;
    private final Map<String, MultiplicityObjectFactory<?>> referenceFactories;

    /**
     * Constructor for a Java Component.
     *
     * @param componentId the component's uri
     * @param instanceFactoryProvider the provider for the instance factory
     * @param scopeContainer the container for the component's implementation scope
     * @param groupId the component group this component belongs to
     * @param initLevel the initialization level
     * @param maxIdleTime the time after which idle instances of this component can be expired
     * @param maxAge the time after which instances of this component can be expired
     * @param proxyService the service used to create reference proxies
     * @param propertyFactories map of factories for property values
     */
    public JavaComponent(URI componentId,
                         InstanceFactoryProvider<T> instanceFactoryProvider,
                         ScopeContainer<?> scopeContainer,
                         URI groupId,
                         int initLevel,
                         long maxIdleTime,
                         long maxAge,
                         ProxyService proxyService,
                         Map<String, ObjectFactory<?>> propertyFactories,
                         Map<String, MultiplicityObjectFactory<?>> referenceFactories,
                         String key) {
        super(componentId, instanceFactoryProvider, scopeContainer, groupId, initLevel, maxIdleTime, maxAge, key);
        this.proxyService = proxyService;
        this.propertyFactories = propertyFactories;
        this.referenceFactories = referenceFactories;
    }

    public <B> B getService(Class<B> businessInterface, String referenceName) {
        throw new UnsupportedOperationException();
    }

    public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, String referenceName) {
        throw new UnsupportedOperationException();
    }

    public <B> B getProperty(Class<B> type, String propertyName) throws ObjectCreationException {
        // TODO for now assume property values will be assignable to the user class - we can add mediation later
        ObjectFactory<?> factory = propertyFactories.get(propertyName);
        if (factory == null) {
            return null;
        } else {
            return type.cast(factory.getInstance());
        }
    }

    @SuppressWarnings("unchecked")
    public <B, R extends CallableReference<B>> R cast(B target) {
        return (R) proxyService.cast(target);
    }
    
    /**
     * Attaches a reference source to the target.
     * 
     * @param referenceSource Reference source.
     * @param objectFactory Object factory.
     * @param target Target component.
     */
    public void attachReferenceToTarget(ValueSource referenceSource, ObjectFactory<?> objectFactory, AtomicComponent<?> target) {
        
        MultiplicityObjectFactory<?> factory = referenceFactories.get(referenceSource.getName());
        if(factory != null) {
            factory.addObjectFactory(objectFactory, target);
            setObjectFactory(referenceSource, factory);
        } else {
            setObjectFactory(referenceSource, objectFactory);
        }
    }
}
