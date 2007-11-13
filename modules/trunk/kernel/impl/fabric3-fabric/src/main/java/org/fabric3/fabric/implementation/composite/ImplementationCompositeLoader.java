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
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.loader.common.InvalidNameException;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.loader.Loader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.MissingResourceException;
import org.fabric3.spi.loader.StAXElementLoader;

/**
 * Loader that handles an &lt;implementation.composite&gt; element.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ImplementationCompositeLoader implements StAXElementLoader<CompositeImplementation> {
    private final Loader loader;
    private final LoaderRegistry registry;

    public ImplementationCompositeLoader(@Reference LoaderRegistry loader) {
        this.loader = loader;
        this.registry = loader;
    }

    @Init
    public void init() {
        registry.registerLoader(CompositeImplementation.IMPLEMENTATION_COMPOSITE, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(CompositeImplementation.IMPLEMENTATION_COMPOSITE);
    }

    public CompositeImplementation load(XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {

        assert CompositeImplementation.IMPLEMENTATION_COMPOSITE.equals(reader.getName());
        String nameAttr = reader.getAttributeValue(null, "name");
        if (nameAttr == null || nameAttr.length() == 0) {
            InvalidNameException e = new InvalidNameException(nameAttr, loaderContext.getSourceBase());
            e.setResourceURI(loaderContext.getSourceBase().toString());
            throw e;
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
                MissingResourceException e2 = new MissingResourceException(scdlLocation, name.toString(), e);
                e2.setResourceURI(loaderContext.getSourceBase().toString());
                throw e2;
            }
        } else {
            if (scdlResource == null) {
                // for now assume the local part is the name
                scdlResource = name.getLocalPart() + ".composite";
            }
            url = cl.getResource(scdlResource);
            if (url == null) {
                MissingResourceException e = new MissingResourceException(scdlResource, name.toString());
                e.setResourceURI(loaderContext.getSourceBase().toString());
                throw e;
            }
        }
        LoaderContext childContext = new LoaderContextImpl(cl, url);
        Composite composite = loader.load(url, Composite.class, childContext);
        CompositeImplementation impl = new CompositeImplementation();
        impl.setName(composite.getName());
        impl.setComponentType(composite);
        return impl;

    }
}
