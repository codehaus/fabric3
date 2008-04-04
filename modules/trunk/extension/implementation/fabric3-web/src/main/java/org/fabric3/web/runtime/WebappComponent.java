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
package org.fabric3.web.runtime;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;

import org.osoa.sca.CallableReference;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.ServiceReference;

import org.fabric3.scdl.PropertyValue;
import org.fabric3.spi.AbstractLifecycle;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.wire.Wire;

/**
 * A component whose implementation is a web applicaiton.
 *
 * @version $Rev: 3020 $ $Date: 2008-03-03 19:16:33 -0800 (Mon, 03 Mar 2008) $
 */
public class WebappComponent<T> extends AbstractLifecycle implements AtomicComponent<T> {
    public static final String CONTEXT_ATTRIBUTE = "fabric3.context";

    private final URI uri;
    private ProxyService proxyService;
    private URI groupId;
    private final Map<String, ObjectFactory<?>> propertyFactories;
    private final Map<String, Class<?>> referenceTypes;
    private final Map<String, ObjectFactory<?>> referenceFactories;
    private final ComponentContext context;
    private final String key;
    private ServletContext servletContext;

    public WebappComponent(URI uri,
                           ServletContext servletContext,
                           ProxyService proxyService,
                           URI groupId,
                           Map<String, ObjectFactory<?>> propertyFactories,
                           Map<String, Class<?>> referenceTypes,
                           String key) throws WebComponentCreationException {
        this.uri = uri;
        this.servletContext = servletContext;
        this.proxyService = proxyService;
        this.groupId = groupId;
        this.propertyFactories = propertyFactories;
        this.referenceTypes = referenceTypes;
        this.key = key;
        referenceFactories = new ConcurrentHashMap<String, ObjectFactory<?>>(referenceTypes.size());
        context = new WebappComponentContext(this);
        servletContext.setAttribute(CONTEXT_ATTRIBUTE, context);
        try {
            // bind properties to the servlet context
            for (Map.Entry<String, ObjectFactory<?>> entry : propertyFactories.entrySet()) {
                servletContext.setAttribute(entry.getKey(), entry.getValue().getInstance());
            }
        } catch (ObjectCreationException e) {
            throw new WebComponentCreationException("Error creating web component: " + uri.toString(), e);
        }
    }

    public URI getUri() {
        return uri;
    }

    public void start() {
    }

    public void stop() {
    }

    public Map<String, PropertyValue> getDefaultPropertyValues() {
        return null;
    }

    public void setDefaultPropertyValues(Map<String, PropertyValue> defaultPropertyValues) {

    }

    public void attachWire(String name, Wire wire) throws ObjectCreationException {
        Class<?> type = referenceTypes.get(name);
        ObjectFactory<?> factory = createWireFactory(type, wire);
        attachWire(name, factory);
    }

    public void attachWire(String name, ObjectFactory<?> factory) throws ObjectCreationException {
        referenceFactories.put(name, factory);
        // bind the reference to the servlet context
        servletContext.setAttribute(name, factory.getInstance());
    }

    protected <B> ObjectFactory<B> createWireFactory(Class<B> interfaze, Wire wire) {
        return proxyService.createObjectFactory(interfaze, InteractionType.STATELESS, wire, null);
    }

    public URI getGroupId() {
        return groupId;
    }

    public boolean isEagerInit() {
        return false;
    }

    public int getInitLevel() {
        return 0;
    }

    public long getMaxIdleTime() {
        return 0;
    }

    public long getMaxAge() {
        return 0;
    }

    public String getKey() {
        return key;
    }

    public InstanceWrapper<T> createInstanceWrapper(WorkContext workContext) throws ObjectCreationException {
        throw new UnsupportedOperationException();
    }

    public ObjectFactory<T> createObjectFactory() {
        throw new UnsupportedOperationException();
    }

    public <R> ObjectFactory<R> createObjectFactory(Class<R> type, String serviceName) throws ObjectCreationException {
        throw new UnsupportedOperationException();
    }

    public <B> B getProperty(Class<B> type, String propertyName) throws ObjectCreationException {
        ObjectFactory<?> factory = propertyFactories.get(propertyName);
        if (factory != null) {
            return type.cast(factory.getInstance());
        } else {
            return null;
        }
    }

    public <B> B getService(Class<B> type, String name) throws ObjectCreationException {
        ObjectFactory<?> factory = referenceFactories.get(name);
        if (factory == null) {
            return null;
        } else {
            return type.cast(factory.getInstance());
        }
    }

    public <B> ServiceReference<B> getServiceReference(Class<B> type, String name) {
        throw new UnsupportedOperationException();
//        ObjectFactory<B> factory = (ObjectFactory<B>) referenceFactories.get(name);
//        if (factory == null) {
//            return null;
//        } else {
//            return new ServiceReferenceImpl<B>(type, factory);
//        }
    }

    @SuppressWarnings({"unchecked"})
    public <B, R extends CallableReference<B>> R cast(B target) {
        return (R) proxyService.cast(target);
    }

    public String toString() {
        return "[" + uri.toString() + "] in state [" + super.toString() + ']';
    }

}
