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
package org.fabric3.itest.implementation.junit;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Constructor;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.spi.Constants;
import org.fabric3.spi.loader.ComponentTypeLoader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;

/**
 * @version $Rev$ $Date$
 */
public class ImplementationJUnitLoader extends LoaderExtension<ImplementationJUnit> {
    private static final QName JUNIT = new QName(Constants.FABRIC3_NS, "junit");

    private final ComponentTypeLoader<ImplementationJUnit> componentTypeLoader;

    @Constructor({"loaderRegistry", "componentTypeLoader"})
    public ImplementationJUnitLoader(@Reference LoaderRegistry registry,
                                     @Reference ComponentTypeLoader<ImplementationJUnit> componentTypeLoader) {
        super(registry);
        this.componentTypeLoader = componentTypeLoader;
    }

    public QName getXMLType() {
        return JUNIT;
    }

    public ImplementationJUnit load(XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {
        String className = reader.getAttributeValue(null, "class");
        LoaderUtil.skipToEndElement(reader);

        ImplementationJUnit impl = new ImplementationJUnit(className);
        componentTypeLoader.load(impl, loaderContext);
        return impl;
    }
}
