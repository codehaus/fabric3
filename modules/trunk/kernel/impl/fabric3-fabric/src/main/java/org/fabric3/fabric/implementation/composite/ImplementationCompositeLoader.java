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
package org.fabric3.fabric.implementation.composite;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.loader.common.IntrospectionContextImpl;
import org.fabric3.loader.common.MissingAttributeException;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderRegistry;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingResourceException;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.MetaDataStoreException;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.ResourceElement;

/**
 * Loader that handles an &lt;implementation.composite&gt; element.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ImplementationCompositeLoader implements TypeLoader<CompositeImplementation> {
    private final Loader loader;
    private final LoaderRegistry registry;
    private final MetaDataStore store;

    public ImplementationCompositeLoader(@Reference LoaderRegistry loader, @Reference MetaDataStore store) {
        this.loader = loader;
        this.registry = loader;
        this.store = store;
    }

    @Init
    public void init() {
        registry.registerLoader(CompositeImplementation.IMPLEMENTATION_COMPOSITE, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(CompositeImplementation.IMPLEMENTATION_COMPOSITE);
    }

    public CompositeImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext)
            throws XMLStreamException, LoaderException {

        assert CompositeImplementation.IMPLEMENTATION_COMPOSITE.equals(reader.getName());
        String nameAttr = reader.getAttributeValue(null, "name");
        String scdlLocation = reader.getAttributeValue(null, "scdlLocation");
        String scdlResource = reader.getAttributeValue(null, "scdlResource");
        LoaderUtil.skipToEndElement(reader);

        ClassLoader cl = introspectionContext.getTargetClassLoader();
        CompositeImplementation impl = new CompositeImplementation();
        URI contributionUri = introspectionContext.getContributionUri();
        URL url;
        if (scdlLocation != null) {
            try {
                url = new URL(introspectionContext.getSourceBase(), scdlLocation);
            } catch (MalformedURLException e) {
                throw new MissingResourceException(scdlLocation, scdlLocation, e);
            }
            IntrospectionContext childContext = new IntrospectionContextImpl(cl, contributionUri, url);
            Composite composite = loader.load(url, Composite.class, childContext);
            impl.setName(composite.getName());
            impl.setComponentType(composite);
            return impl;
        } else if (scdlResource != null) {
            url = cl.getResource(scdlResource);
            if (url == null) {
                throw new MissingResourceException(scdlResource, scdlResource);
            }
            IntrospectionContext childContext = new IntrospectionContextImpl(cl, contributionUri, url);
            Composite composite = loader.load(url, Composite.class, childContext);
            impl.setName(composite.getName());
            impl.setComponentType(composite);
            return impl;
        } else if (nameAttr != null) {
            String targetNamespace = introspectionContext.getTargetNamespace();
            NamespaceContext namespaceContext = reader.getNamespaceContext();
            QName name = LoaderUtil.getQName(nameAttr, targetNamespace, namespaceContext);
            QNameSymbol symbol = new QNameSymbol(name);
            try {
                ResourceElement<QNameSymbol, Composite> element =
                        store.resolve(contributionUri, Composite.class, symbol);
                if (element == null) {
                    String identifier = name.toString();
                    throw new MissingResourceException("Composite not found [" + identifier + "]", identifier);
                }
                impl.setComponentType(element.getValue());
            } catch (MetaDataStoreException e) {
                throw new LoaderException(e);
            }

            return impl;

        } else {
            throw new MissingAttributeException("Implementaiton.composite must have a name, scdlLocation, or scdlResource");
        }

    }
}
