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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.Constants;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.loader.common.InvalidNameException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.scdl.CompositeImplementation;

/**
 * Loader that handles an &lt;implementation.composite&gt; element.
 *
 * @version $Rev$ $Date$
 */
public class ImplementationCompositeLoader extends LoaderExtension<CompositeImplementation> {
    private static final QName IMPLEMENTATION_COMPOSITE =
            new QName(Constants.SCA_NS, "implementation.composite");

    public ImplementationCompositeLoader(@Reference LoaderRegistry registry) {
        super(registry);
    }

    public QName getXMLType() {
        return IMPLEMENTATION_COMPOSITE;
    }

    public CompositeImplementation load(XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {

        assert IMPLEMENTATION_COMPOSITE.equals(reader.getName());
        String nameAttr = reader.getAttributeValue(null, "name");
        if (nameAttr == null || nameAttr.length() == 0) {
            throw new InvalidNameException(nameAttr);
        }
        QName name = LoaderUtil.getQName(nameAttr, loaderContext.getTargetNamespace(), reader.getNamespaceContext());
        LoaderUtil.skipToEndElement(reader);
        CompositeImplementation impl = new CompositeImplementation();
        impl.setName(name);
        return impl;
    }
}
