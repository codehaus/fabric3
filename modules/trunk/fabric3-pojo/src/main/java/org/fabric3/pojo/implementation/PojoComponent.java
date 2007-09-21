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
package org.fabric3.pojo.implementation;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;

import org.fabric3.pojo.ComponentObjectFactory;
import org.fabric3.pojo.injection.MultiplicityObjectFactory;
import org.fabric3.scdl.PropertyValue;
import org.fabric3.spi.AbstractLifecycle;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceFactory;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.model.instance.ValueSource;
import org.osoa.sca.ComponentContext;

/**
 * Base class for Component implementations based on Java objects.
 *
 * @version $Rev$ $Date$
 * @param <T> the implementation class
 */
public abstract class PojoComponent<T> extends AbstractLifecycle implements AtomicComponent<T> {
    private final URI uri;
    private final InstanceFactoryProvider<T> provider;
    private final ScopeContainer<?> scopeContainer;
    private final URI groupId;
    private final int initLevel;
    private final long maxIdleTime;
    private final long maxAge;
    private InstanceFactory<T> instanceFactory;
    private final Map<String, MultiplicityObjectFactory<?>> referenceFactories;

    public PojoComponent(URI componentId,
                         InstanceFactoryProvider<T> provider,
                         ScopeContainer<?> scopeContainer,
                         URI groupId,
                         int initLevel,
                         long maxIdleTime,
                         long maxAge,
                         Map<String, MultiplicityObjectFactory<?>> referenceFactories) {
        this.uri = componentId;
        this.provider = provider;
        this.scopeContainer = scopeContainer;
        this.groupId = groupId;
        this.initLevel = initLevel;
        this.maxIdleTime = maxIdleTime;
        this.maxAge = maxAge;
        this.referenceFactories = referenceFactories;
    }

    public URI getUri() {
        return uri;
    }

    public URI getGroupId() {
        return groupId;
    }

    public boolean isEagerInit() {
        return initLevel > 0;
    }

    public int getInitLevel() {
        return initLevel;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void start() {
        super.start();
        instanceFactory = provider.createFactory();
        scopeContainer.register(this);
    }

    public void stop() {
        instanceFactory = null;
        scopeContainer.unregister(this);
        super.stop();
    }

    public InstanceWrapper<T> createInstanceWrapper(WorkContext workContext) throws ObjectCreationException {
        return instanceFactory.newInstance(workContext);
    }

    @SuppressWarnings({"unchecked"})
    public ObjectFactory<T> createObjectFactory() {
        return new ComponentObjectFactory(this, scopeContainer);
    }

    public ComponentContext getComponentContext() {
        return null;
    }

    public Map<String, PropertyValue> getDefaultPropertyValues() {
        return null;
    }

    public void setDefaultPropertyValues(Map<String, PropertyValue> defaultPropertyValues) {
    }

    public ScopeContainer getScopeContainer() {
        return scopeContainer;
    }

    public Class<T> getImplementationClass() {
        return provider.getImplementationClass();
    }

    public void setObjectFactory(ValueSource name, ObjectFactory<?> objectFactory) {
        provider.setObjectFactory(name, objectFactory);
    }

    public Class<?> getMemberType(ValueSource injectionSite) {
        return provider.getMemberType(injectionSite);
    }
    
    public Type getGerenricMemberType(ValueSource injectionSite) {
        return provider.getMemberType(injectionSite);
    }

    public String toString() {
        return "[" + uri.toString() + "] in state [" + super.toString() + ']';
    }
    
    /**
     * Attaches a reference source to the target.
     * 
     * @param referenceSource Reference source.
     * @param objectFactory Object factory.
     * @param target Target component.
     */
    public void attachReferenceToTarget(ValueSource referenceSource, ObjectFactory<?> objectFactory, Object key) {
        
        MultiplicityObjectFactory<?> factory = referenceFactories.get(referenceSource.getName());
        if(factory != null) {
            factory.addObjectFactory(objectFactory, key);
            setObjectFactory(referenceSource, factory);
        } else {
            setObjectFactory(referenceSource, objectFactory);
        }
    }

}
