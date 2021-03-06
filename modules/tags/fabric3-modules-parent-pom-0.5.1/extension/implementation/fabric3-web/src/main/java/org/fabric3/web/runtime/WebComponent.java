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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osoa.sca.CallableReference;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.ServiceReference;

import org.fabric3.container.web.spi.WebApplicationActivationException;
import org.fabric3.container.web.spi.WebApplicationActivator;
import static org.fabric3.container.web.spi.WebApplicationActivator.CONTEXT_ATTRIBUTE;
import org.fabric3.pojo.reflection.Injector;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.PropertyValue;
import org.fabric3.spi.AbstractLifecycle;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.services.proxy.ProxyService;
import org.fabric3.spi.wire.Wire;

/**
 * A component whose implementation is a web applicaiton.
 *
 * @version $Rev: 3020 $ $Date: 2008-03-03 19:16:33 -0800 (Mon, 03 Mar 2008) $
 */
public class WebComponent<T> extends AbstractLifecycle implements AtomicComponent<T> {

    private final URI uri;
    private final URI classLoaderId;
    private ClassLoader classLoader;
    private InjectorFactory injectorFactory;
    private final WebApplicationActivator activator;
    // injection site name to <artifact name, injection site>
    private final Map<String, Map<String, InjectionSite>> siteMappings;
    private final ProxyService proxyService;
    private final URI groupId;
    private final Map<String, ObjectFactory<?>> propertyFactories;
    private final Map<String, ObjectFactory<?>> referenceFactories;
    private final URI archiveUri;
    private ComponentContext context;
    private String contextUrl;

    public WebComponent(URI uri,
                        String contextUrl,
                        URI classLoaderId,
                        URI groupId,
                        URI archiveUri,
                        ClassLoader classLoader,
                        InjectorFactory injectorFactory,
                        WebApplicationActivator activator,
                        ProxyService proxyService,
                        Map<String, ObjectFactory<?>> propertyFactories,
                        Map<String, Map<String, InjectionSite>> injectorMappings) throws WebComponentCreationException {
        this.uri = uri;
        this.contextUrl = contextUrl;
        this.classLoaderId = classLoaderId;
        this.archiveUri = archiveUri;
        this.classLoader = classLoader;
        this.injectorFactory = injectorFactory;
        this.activator = activator;
        this.siteMappings = injectorMappings;
        this.proxyService = proxyService;
        this.groupId = groupId;
        this.propertyFactories = propertyFactories;
        referenceFactories = new ConcurrentHashMap<String, ObjectFactory<?>>();
    }

    public URI getUri() {
        return uri;
    }

    public void start() {
        try {
            Map<String, List<Injector<?>>> injectors = new HashMap<String, List<Injector<?>>>();
            injectorFactory.createInjectorMappings(injectors, siteMappings, referenceFactories, classLoader);
            injectorFactory.createInjectorMappings(injectors, siteMappings, propertyFactories, classLoader);
            context = new WebComponentContext(this);
            Map<String, ObjectFactory<?>> contextFactories = new HashMap<String, ObjectFactory<?>>();
            SingletonObjectFactory<ComponentContext> componentContextFactory = new SingletonObjectFactory<ComponentContext>(context);
            contextFactories.put(CONTEXT_ATTRIBUTE, componentContextFactory);
            injectorFactory.createInjectorMappings(injectors, siteMappings, contextFactories, classLoader);
            // activate the web application
            activator.activate(contextUrl, archiveUri, classLoaderId, injectors, context);
        } catch (InjectionCreationException e) {
            throw new WebComponentStartException("Error starting web component: " + uri.toString(), e);
        } catch (WebApplicationActivationException e) {
            throw new WebComponentStartException("Error starting web component: " + uri.toString(), e);
        }

    }

    public void stop() {
    }

    public Map<String, PropertyValue> getDefaultPropertyValues() {
        return null;
    }

    public void setDefaultPropertyValues(Map<String, PropertyValue> defaultPropertyValues) {

    }

    public void attachWire(String name, InteractionType interactionType, Wire wire) throws ObjectCreationException {
        Map<String, InjectionSite> sites = siteMappings.get(name);
        if (sites == null || sites.isEmpty()) {
            throw new ObjectCreationException("Injection site not found for: " + name);
        }
        Class<?> type;
        try {
            type = classLoader.loadClass(sites.values().iterator().next().getType());
        } catch (ClassNotFoundException e) {
            throw new ObjectCreationException("Reference type not found for: " + name, e);
        }
        ObjectFactory<?> factory = createWireFactory(type, interactionType, wire);
        attachWire(name, factory);
    }

    public void attachWire(String name, ObjectFactory<?> factory) throws ObjectCreationException {
        referenceFactories.put(name, factory);
    }

    protected <B> ObjectFactory<B> createWireFactory(Class<B> interfaze, InteractionType interactionType, Wire wire) {
        return proxyService.createObjectFactory(interfaze, interactionType, wire, null);
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
    }

    @SuppressWarnings({"unchecked"})
    public <B, R extends CallableReference<B>> R cast(B target) {
        return (R) proxyService.cast(target);
    }

    public String toString() {
        return "[" + uri.toString() + "] in state [" + super.toString() + ']';
    }


}
