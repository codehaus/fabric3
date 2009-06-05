/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.StoreException;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.ElementLoadFailure;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Loader that handles an &lt;implementation.composite&gt; element.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ImplementationCompositeLoader extends AbstractExtensibleTypeLoader<CompositeImplementation> {
    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();
    private static final QName IMPL = new QName(Constants.SCA_NS, "implementation.composite");

    static {
        ATTRIBUTES.put("name", "name");
        ATTRIBUTES.put("scdlLocation", "scdlLocation");
        ATTRIBUTES.put("scdlResource", "scdlResource");
        ATTRIBUTES.put("requires", "requires");
    }

    private final MetaDataStore store;

    public ImplementationCompositeLoader(@Reference LoaderRegistry registry, @Reference MetaDataStore store) {
        super(registry);
        this.store = store;
    }

    public QName getXMLType() {
        return IMPL;
    }

    public CompositeImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        assert CompositeImplementation.IMPLEMENTATION_COMPOSITE.equals(reader.getName());
        validateAttributes(reader, introspectionContext);
        // read name now b/c the reader skips ahead
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
                MissingComposite failure = new MissingComposite("Composite file not found: " + scdlLocation, reader);
                introspectionContext.addError(failure);
                return impl;
            }
            IntrospectionContext childContext = new DefaultIntrospectionContext(cl, contributionUri, url);
            Composite composite;
            try {
                composite = registry.load(url, Composite.class, childContext);
                if (childContext.hasErrors()) {
                    introspectionContext.addErrors(childContext.getErrors());
                }
                if (childContext.hasWarnings()) {
                    introspectionContext.addWarnings(childContext.getWarnings());
                }
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
                MissingComposite failure = new MissingComposite("Composite file not found: " + scdlResource, reader);
                introspectionContext.addError(failure);
                return impl;
            }
            IntrospectionContext childContext = new DefaultIntrospectionContext(cl, contributionUri, url);
            Composite composite;
            try {
                composite = registry.load(url, Composite.class, childContext);
                if (childContext.hasErrors()) {
                    introspectionContext.addErrors(childContext.getErrors());
                }
                if (childContext.hasWarnings()) {
                    introspectionContext.addWarnings(childContext.getWarnings());
                }
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
            if (nameAttr == null || nameAttr.length() == 0) {
                MissingAttribute failure = new MissingAttribute("Missing name attribute", reader);
                introspectionContext.addError(failure);
                return null;
            }
            QName name = LoaderUtil.getQName(nameAttr, introspectionContext.getTargetNamespace(), reader.getNamespaceContext());

            try {
                QNameSymbol symbol = new QNameSymbol(name);
                ResourceElement<QNameSymbol, Composite> element = store.resolve(contributionUri, Composite.class, symbol, introspectionContext);
                if (element == null) {
                    String id = name.toString();
                    MissingComposite failure = new MissingComposite("Composite with qualified name not found: " + id, reader);
                    introspectionContext.addError(failure);
                    return impl;
                }
                impl.setComponentType(element.getValue());
                return impl;
            } catch (StoreException e) {
                ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, reader);
                introspectionContext.addError(failure);
                return null;

            }
        }

    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!ATTRIBUTES.containsKey(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}
