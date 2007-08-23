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

import java.net.URL;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.loader.common.InvalidNameException;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.MissingResourceException;

/**
 * Loader that handles an &lt;implementation.composite&gt; element.
 *
 * @version $Rev$ $Date$
 */
public class ImplementationCompositeLoader extends LoaderExtension<CompositeImplementation> {

    public ImplementationCompositeLoader(@Reference LoaderRegistry registry) {
        super(registry);
    }

    public QName getXMLType() {
        return CompositeImplementation.IMPLEMENTATION_COMPOSITE;
    }

    public CompositeImplementation load(XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {

        assert CompositeImplementation.IMPLEMENTATION_COMPOSITE.equals(reader.getName());
        String nameAttr = reader.getAttributeValue(null, "name");
        if (nameAttr == null || nameAttr.length() == 0) {
            throw new InvalidNameException(nameAttr);
        }
        String namespace = loaderContext.getTargetNamespace();
        NamespaceContext namespaceContext = reader.getNamespaceContext();
        QName name = LoaderUtil.getQName(nameAttr, namespace, namespaceContext);
        // for now, assume file name is the local name part
        String file = name.getLocalPart() + ".composite";
        URL url = loaderContext.getTargetClassLoader().getResource(file);
        if (url == null) {
            throw new MissingResourceException("Composite file not found", file);
        }
        Composite composite = registry.load(url, Composite.class, loaderContext);
        LoaderUtil.skipToEndElement(reader);
        CompositeImplementation impl = new CompositeImplementation();
        impl.setName(composite.getName());
        impl.setComponentType(composite);
        return impl;

    }
}
