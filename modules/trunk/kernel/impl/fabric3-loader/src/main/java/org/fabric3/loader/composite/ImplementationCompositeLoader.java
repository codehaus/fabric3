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
package org.fabric3.loader.composite;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.ElementLoadFailure;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderRegistry;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
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

    public ImplementationCompositeLoader(@Reference LoaderRegistry loader, @Reference(required = false)MetaDataStore store) {
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

    public CompositeImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {

        assert CompositeImplementation.IMPLEMENTATION_COMPOSITE.equals(reader.getName());
        String nameAttr = reader.getAttributeValue(null, "name");
        if (nameAttr == null || nameAttr.length() == 0) {
            MissingAttribute failure = new MissingAttribute("Missing name attribute", "name", reader);
            introspectionContext.addError(failure);
            return null;
        }
        QName name = LoaderUtil.getQName(nameAttr, introspectionContext.getTargetNamespace(), reader.getNamespaceContext());
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
                MissingComposite failure = new MissingComposite("Composite file not found: " + scdlLocation, scdlLocation, reader);
                introspectionContext.addError(failure);
                return impl;
            }
            IntrospectionContext childContext = new DefaultIntrospectionContext(cl, contributionUri, url);
            Composite composite;
            try {
                composite = loader.load(url, Composite.class, childContext);
                if (composite == null) {
                    // error loading composite, return
                    return null;
                }
            } catch (LoaderException e) {
                ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, reader);
                introspectionContext.addError(failure);
                return null;
            }
            impl.setName(composite.getName());
            impl.setComponentType(composite);
            return impl;
        } else if (scdlResource != null) {
            url = cl.getResource(scdlResource);
            if (url == null) {
                MissingComposite failure = new MissingComposite("Composite file not found: " + scdlResource, scdlResource, reader);
                introspectionContext.addError(failure);
                return impl;
            }
            IntrospectionContext childContext = new DefaultIntrospectionContext(cl, contributionUri, url);
            Composite composite = null;
            try {
                composite = loader.load(url, Composite.class, childContext);
                if (composite == null) {
                    // error loading composite, return
                    return null;
                }
            } catch (LoaderException e) {
                ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, reader);
                introspectionContext.addError(failure);
                return null;
            }
            impl.setName(composite.getName());
            impl.setComponentType(composite);
            return impl;
        } else {
            if (store == null) {
                // throw error as this is invalid in a bootstrap environment
                throw new UnsupportedOperationException("scdlLocation or scdlResource must be supplied as no MetaDataStore is available");
            }

            try {
                QNameSymbol symbol = new QNameSymbol(name);
                ResourceElement<QNameSymbol, Composite> element = store.resolve(contributionUri, Composite.class, symbol);
                if (element == null) {
                    String id = name.toString();
                    MissingComposite failure = new MissingComposite("Composite with qualified name not found: " + id, id, reader);
                    introspectionContext.addError(failure);
                    return impl;
                }
                impl.setComponentType(element.getValue());
                return impl;
            } catch (MetaDataStoreException e) {
                ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, reader);
                introspectionContext.addError(failure);
                return null;

            }
        }

    }

}
