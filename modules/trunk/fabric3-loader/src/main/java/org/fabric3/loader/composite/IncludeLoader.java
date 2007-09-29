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
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
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

/**
 * Loader that handles &lt;include&gt; elements.
 *
 * @version $Rev$ $Date$
 */
public class IncludeLoader implements StAXElementLoader<Include> {
    private static final QName INCLUDE = new QName(SCA_NS, "Include");

    private final Loader registry;

    public IncludeLoader(@Reference Loader registry) {
        this.registry = registry;
    }

    public QName getXMLType() {
        return INCLUDE;
    }

    public Include load(XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {

        String nameAttr = reader.getAttributeValue(null, "name");
        if (nameAttr == null || nameAttr.length() == 0) {
            throw new InvalidNameException(nameAttr);
        }
        QName name = LoaderUtil.getQName(nameAttr, loaderContext.getTargetNamespace(), reader.getNamespaceContext());
        String scdlLocation = reader.getAttributeValue(null, "scdlLocation");
        String scdlResource = reader.getAttributeValue(null, "scdlResource");
        LoaderUtil.skipToEndElement(reader);

        ClassLoader cl = loaderContext.getTargetClassLoader();
        URL url;
        if (scdlLocation != null) {
            try {
                url = new URL(loaderContext.getSourceBase(), scdlLocation);
            } catch (MalformedURLException e) {
                throw new MissingResourceException(scdlLocation, name.toString(), e);
            }
        } else if (scdlResource != null) {
            url = cl.getResource(scdlResource);
            if (url == null) {
                throw new MissingResourceException(scdlResource, name.toString());
            }
        } else {
            throw new MissingIncludeException("No SCDL location or resource specified", name.toString());
        }

        LoaderContext childContext =
                new LoaderContextImpl(cl, url);
        Composite composite;
        composite = loadFromSidefile(url, childContext);

        Include include = new Include();
        include.setName(name);
        include.setScdlLocation(url);
        include.setIncluded(composite);
        return include;
    }

    protected Composite loadFromSidefile(URL url, LoaderContext context) throws LoaderException {
        return registry.load(url, Composite.class, context);
    }
}
