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
package org.fabric3.fabric.implementation.singleton;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.osoa.sca.ComponentContext;

import org.fabric3.spi.AbstractLifecycle;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.idl.java.JavaServiceContract;
import org.fabric3.scdl.PropertyValue;

/**
 * An {@link org.fabric3.spi.component.AtomicComponent} used when registering objects directly into a composite
 *
 * @version $$Rev$$ $$Date$$
 */
public class SingletonComponent<T> extends AbstractLifecycle implements AtomicComponent<T> {
    private final URI uri;
    private T instance;
    private List<JavaServiceContract<?>> serviceContracts = new ArrayList<JavaServiceContract<?>>();
    private Map<String, PropertyValue> defaultPropertyValues;

    public SingletonComponent(URI componentId, List<JavaServiceContract<?>> services, T instance) {
        this.uri = componentId;
        this.instance = instance;
        this.serviceContracts.addAll(services);
    }

    public URI getUri() {
        return uri;
    }

    public URI getGroupId() {
        return null;
    }

    public boolean isEagerInit() {
        return false;
    }

    public int getInitLevel() {
        return 0;
    }

    public long getMaxIdleTime() {
        return -1;
    }

    public long getMaxAge() {
        return -1;
    }

    public InstanceWrapper<T> createInstanceWrapper(WorkContext workContext) throws ObjectCreationException {
        throw new UnsupportedOperationException();
    }

    public ObjectFactory<T> createObjectFactory() {
        return new SingletonObjectFactory<T>(instance);
    }

    public ComponentContext getComponentContext() {
        // singleton components do not give out a component context
        return null;
    }

    public Map<String, PropertyValue> getDefaultPropertyValues() {
        return defaultPropertyValues;
    }

    public void setDefaultPropertyValues(Map<String, PropertyValue> defaultPropertyValues) {
        this.defaultPropertyValues = defaultPropertyValues;
    }

    public List<JavaServiceContract<?>> getServiceContracts() {
        return serviceContracts;
    }

    public String toString() {
        return "[" + uri.toString() + "] in state [" + super.toString() + ']';
    }


}
