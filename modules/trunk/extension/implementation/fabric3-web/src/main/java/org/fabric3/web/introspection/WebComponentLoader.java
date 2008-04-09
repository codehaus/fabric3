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
package org.fabric3.web.introspection;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderRegistry;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.scdl.ComponentType;

/**
 * Loads <code><implementation.web></code> from a composite.
 *
 * @version $Rev: 3105 $ $Date: 2008-03-15 09:47:31 -0700 (Sat, 15 Mar 2008) $
 */
@EagerInit
public class WebComponentLoader implements TypeLoader<WebImplementation> {

    private final LoaderRegistry registry;

    public WebComponentLoader(@Reference LoaderRegistry registry
    ) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.registerLoader(WebImplementation.IMPLEMENTATION_WEBAPP, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(WebImplementation.IMPLEMENTATION_WEBAPP);
    }

    public WebImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext)
            throws XMLStreamException, LoaderException {

        ComponentType componentType = loadComponentType(introspectionContext);
        WebImplementation impl = new WebImplementation();
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
        IntrospectionContext childContext = new DefaultIntrospectionContext(context.getTargetClassLoader(), null, url);
        ComponentType componentType = registry.load(url, ComponentType.class, childContext);
        componentType.setScope("COMPOSITE");
        return componentType;
    }
}
