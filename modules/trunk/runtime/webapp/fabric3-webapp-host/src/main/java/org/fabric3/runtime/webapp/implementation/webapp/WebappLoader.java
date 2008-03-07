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

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.loader.common.IntrospectionContextImpl;
import org.fabric3.scdl.ComponentType;
import org.fabric3.scdl.Scope;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderRegistry;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.TypeLoader;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class WebappLoader implements TypeLoader<WebappImplementation> {

    private final LoaderRegistry registry;

    public WebappLoader(@Reference LoaderRegistry registry
    ) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.registerLoader(WebappImplementation.IMPLEMENTATION_WEBAPP, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(WebappImplementation.IMPLEMENTATION_WEBAPP);
    }

    public WebappImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext)
            throws XMLStreamException, LoaderException {

        ComponentType componentType = loadComponentType(introspectionContext);
        WebappImplementation impl = new WebappImplementation();
        impl.setComponentType(componentType);
        LoaderUtil.skipToEndElement(reader);
        return impl;
    }

    private ComponentType loadComponentType(IntrospectionContext context) throws LoaderException {
        URL url;
        try {
            url = new URL(context.getSourceBase(), "web.componentType");
        } catch (MalformedURLException e) {
            throw new LoaderException(e.getMessage(), e);
        }
        IntrospectionContext childContext = new IntrospectionContextImpl(context.getTargetClassLoader(), null, url);
        ComponentType componentType = registry.load(url, ComponentType.class, childContext);
        componentType.setImplementationScope(Scope.COMPOSITE);
        return componentType;
    }
}
