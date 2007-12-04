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

import org.osoa.sca.annotations.Reference;

import org.fabric3.loader.common.InvalidNameException;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Include;
import org.fabric3.spi.loader.Loader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.MissingResourceException;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.MetaDataStoreException;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.ResourceElement;

/**
 * Loader that handles &lt;include&gt; elements.
 *
 * @version $Rev$ $Date$
 */
public class IncludeLoader implements StAXElementLoader<Include> {
    private final Loader loader;
    private MetaDataStore store;

    public IncludeLoader(@Reference Loader loader, @Reference MetaDataStore store) {
        this.loader = loader;
        this.store = store;
    }

    public Include load(XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {

        String nameAttr = reader.getAttributeValue(null, "name");
        if (nameAttr == null || nameAttr.length() == 0) {
            throw new InvalidNameException(nameAttr, loaderContext.getSourceBase());
        }
        QName name = LoaderUtil.getQName(nameAttr, loaderContext.getTargetNamespace(), reader.getNamespaceContext());
        String scdlLocation = reader.getAttributeValue(null, "scdlLocation");
        String scdlResource = reader.getAttributeValue(null, "scdlResource");
        LoaderUtil.skipToEndElement(reader);

        ClassLoader cl = loaderContext.getTargetClassLoader();
        URI contributionUri = loaderContext.getContributionUri();
        URL url;
        if (scdlLocation != null) {
            try {
                url = new URL(loaderContext.getSourceBase(), scdlLocation);
                return loadFromSideFile(name, cl,contributionUri, url);
            } catch (MalformedURLException e) {
                MissingResourceException e2 = new MissingResourceException(scdlLocation, name.toString(), e);
                e2.setResourceURI(loaderContext.getSourceBase().toString());
                throw e2;
            }
        } else if (scdlResource != null) {
            url = cl.getResource(scdlResource);
            if (url == null) {
                throw new MissingResourceException(scdlResource, name.toString());
            }
            return loadFromSideFile(name, cl,contributionUri, url);
        } else {
            try {
                QNameSymbol symbol = new QNameSymbol(name);
                ResourceElement<QNameSymbol, Include> element = store.resolve(contributionUri, Include.class, symbol);
                if (element == null) {
                    String identifier = name.toString();
                    throw new MissingResourceException("Composite not found [" + identifier + "]", identifier);
                }
                return element.getValue();
            } catch (MetaDataStoreException e) {
                throw new LoaderException(e);
            }
        }
    }

    private Include loadFromSideFile(QName name, ClassLoader cl, URI contributionUri, URL url) throws InvalidIncludeException {
        Include include = new Include();
        LoaderContext childContext = new LoaderContextImpl(cl, contributionUri, url);
        Composite composite;
        try {
            composite = loader.load(url, Composite.class, childContext);
        } catch (LoaderException e) {
            throw new InvalidIncludeException(name, e);
        }

        include.setName(name);
        include.setScdlLocation(url);
        include.setIncluded(composite);
        return include;
    }

}
