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

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.ElementLoadFailure;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.Include;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.host.contribution.StoreException;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.contribution.ResourceElement;

/**
 * Loader that handles &lt;include&gt; elements.
 *
 * @version $Rev$ $Date$
 */
public class IncludeLoader implements TypeLoader<Include> {
    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();

    static {
        ATTRIBUTES.put("name", "name");
        ATTRIBUTES.put("scdlLocation", "scdlLocation");
        ATTRIBUTES.put("scdlResource", "scdlResource");
        ATTRIBUTES.put("requires", "requires");
    }

    private final Loader loader;
    private MetaDataStore store;

    public IncludeLoader(@Reference Loader loader, @Reference(required = false)MetaDataStore store) {
        this.loader = loader;
        this.store = store;
    }

    public Include load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);

        String nameAttr = reader.getAttributeValue(null, "name");
        if (nameAttr == null || nameAttr.length() == 0) {
            MissingAttribute failure = new MissingAttribute("Missing name attribute", "name", reader);
            context.addError(failure);
            return null;
        }
        QName name = LoaderUtil.getQName(nameAttr, context.getTargetNamespace(), reader.getNamespaceContext());
        String scdlLocation = reader.getAttributeValue(null, "scdlLocation");
        String scdlResource = reader.getAttributeValue(null, "scdlResource");
        LoaderUtil.skipToEndElement(reader);

        ClassLoader cl = context.getTargetClassLoader();
        URI contributionUri = context.getContributionUri();
        URL url;
        if (scdlLocation != null) {
            try {
                url = new URL(context.getSourceBase(), scdlLocation);
                return loadFromSideFile(name, cl, contributionUri, url, reader, context);
            } catch (MalformedURLException e) {
                MissingComposite failure = new MissingComposite("Error parsing composite url: " + scdlResource, scdlResource, reader);
                context.addError(failure);
                return null;
            }
        } else if (scdlResource != null) {
            url = cl.getResource(scdlResource);
            if (url == null) {
                MissingComposite failure = new MissingComposite("Composite file not found: " + scdlResource, scdlResource, reader);
                context.addError(failure);
                return null;
            }
            return loadFromSideFile(name, cl, contributionUri, url, reader, context);
        } else {
            if (store == null) {
                // throw error as this is invalid in a bootstrap environment
                throw new UnsupportedOperationException("scdlLocation or scdlResource must be supplied as no MetaDataStore is available");
            }

            try {
                QNameSymbol symbol = new QNameSymbol(name);
                ResourceElement<QNameSymbol, Composite> element = store.resolve(contributionUri, Composite.class, symbol, context);
                if (element == null) {
                    String id = name.toString();
                    MissingComposite failure = new MissingComposite("Composite file not found: " + id, id, reader);
                    context.addError(failure);
                    return null;
                }
                Composite composite = element.getValue();
                Include include = new Include();
                include.setName(name);
                include.setIncluded(composite);
                return include;
            } catch (StoreException e) {
                ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, reader);
                context.addError(failure);
                return null;
            }
        }
    }

    private Include loadFromSideFile(QName name, ClassLoader cl, URI contributionUri, URL url, XMLStreamReader reader, IntrospectionContext context) {
        Include include = new Include();
        IntrospectionContext childContext = new DefaultIntrospectionContext(cl, contributionUri, url);
        Composite composite;
        try {
            composite = loader.load(url, Composite.class, childContext);
        } catch (LoaderException e) {
            InvalidValue failure = new InvalidValue("Error loading include", null, reader);
            context.addError(failure);
            e.printStackTrace();
            return include;
        }
        if (childContext.hasErrors()) {
            context.addErrors(childContext.getErrors());
        }
        if (childContext.hasWarnings()) {
            context.addWarnings(childContext.getWarnings());
        }
        include.setName(name);
        include.setScdlLocation(url);
        include.setIncluded(composite);
        return include;
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
