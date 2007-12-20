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
package org.fabric3.runtime.webapp.implementation.webapp;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.naming.Context;
import javax.naming.NamingException;

import org.osoa.sca.CallableReference;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.ServiceReference;

import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.fabric.component.ServiceReferenceImpl;
import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.runtime.webapp.Constants;
import org.fabric3.scdl.PropertyValue;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.AbstractLifecycle;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
public class WebappComponent<T> extends AbstractLifecycle implements AtomicComponent<T>, ServletRequestListener {
    private final URI uri;
    private ProxyService proxyService;
    private URI groupId;
    private final Map<String, ObjectFactory<?>> propertyFactories;
    private final Map<String, Class<?>> referenceTypes;
    private final Map<String, Wire> referenceFactories;
    private final ComponentContext context;
    private final String key;

    public WebappComponent(URI uri,
                           ProxyService proxyService,
                           URI groupId,
                           Map<String, ObjectFactory<?>> attributes,
                           Map<String, Class<?>> referenceTypes,
                           String key) {
        this.uri = uri;
        this.proxyService = proxyService;
        this.groupId = groupId;
        this.propertyFactories = attributes;
        this.referenceTypes = referenceTypes;
        this.key = key;
        referenceFactories = new ConcurrentHashMap<String, Wire>(referenceTypes.size());
        context = new WebappComponentContext(this);
    }

    public URI getUri() {
        return uri;
    }

    public void start() {
    }

    public void stop() {
    }

    public void requestInitialized(ServletRequestEvent sre) {
        WorkContext workContext = new SimpleWorkContext();
        workContext.setScopeIdentifier(Scope.COMPOSITE, groupId);
        PojoWorkContextTunnel.setThreadWorkContext(workContext);
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        PojoWorkContextTunnel.setThreadWorkContext(null);
    }

    public Map<String, PropertyValue> getDefaultPropertyValues() {
        return null;
    }

    public void setDefaultPropertyValues(Map<String, PropertyValue> defaultPropertyValues) {

    }

    public void attachWire(Wire wire) {
        String name = wire.getSourceUri().getFragment();
        referenceFactories.put(name, wire);
    }

    protected <B> ObjectFactory<B> createWireFactory(Class<B> interfaze, Wire wire) {
        return proxyService.createObjectFactory(interfaze, false, wire);
    }

    public void bind(ServletContext servletContext) throws ObjectCreationException {
        servletContext.setAttribute(Constants.CONTEXT_ATTRIBUTE, getComponentContext());
        for (Map.Entry<String, ObjectFactory<?>> entry : propertyFactories.entrySet()) {
            servletContext.setAttribute(entry.getKey(), entry.getValue().getInstance());
        }
        for (Map.Entry<String, Wire> entry : referenceFactories.entrySet()) {
            String name = entry.getKey();
            Wire wire = entry.getValue();
            Class<?> type = referenceTypes.get(name);
            ObjectFactory<?> factory = createWireFactory(type, wire);
            servletContext.setAttribute(name, factory.getInstance());
        }
    }

    public void bind(Context ctx) throws NamingException, ObjectCreationException {
        ctx.bind(Constants.CONTEXT_ATTRIBUTE, getComponentContext());
        for (Map.Entry<String, ObjectFactory<?>> entry : propertyFactories.entrySet()) {
            ctx.bind(entry.getKey(), entry.getValue().getInstance());
        }
        for (Map.Entry<String, Wire> entry : referenceFactories.entrySet()) {
            String name = entry.getKey();
            Wire wire = entry.getValue();
            Class<?> type = referenceTypes.get(name);
            ObjectFactory<?> factory = createWireFactory(type, wire);
            ctx.bind(name, factory.getInstance());
        }
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

    public ComponentContext getComponentContext() {
        return context;
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
        Wire wire = referenceFactories.get(name);
        if (wire == null) {
            return null;
        }
        ObjectFactory<B> factory = createWireFactory(type, wire);
        return factory.getInstance();
    }

    public <B> ServiceReference<B> getServiceReference(Class<B> type, String name) {
        Wire wire = referenceFactories.get(name);
        if (wire == null) {
            return null;
        }
        ObjectFactory<B> factory = createWireFactory(type, wire);
        return new ServiceReferenceImpl<B>(type, factory);
    }

    @SuppressWarnings({"unchecked"})
    public <B, R extends CallableReference<B>> R cast(B target) {
        return (R) proxyService.cast(target);
    }

    public String toString() {
        return "[" + uri.toString() + "] in state [" + super.toString() + ']';
    }

}
