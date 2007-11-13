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
package org.fabric3.junit;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.StAXElementLoader;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class ImplementationJUnitLoader implements StAXElementLoader<ImplementationJUnit> {

    private final LoaderRegistry registry;
    private final JUnitComponentTypeLoader componentTypeLoader;

    public ImplementationJUnitLoader(@Reference LoaderRegistry registry,
                                     @Reference JUnitComponentTypeLoader componentTypeLoader) {
        this.registry = registry;
        this.componentTypeLoader = componentTypeLoader;
    }

    @Init
    public void init() {
        registry.registerLoader(ImplementationJUnit.IMPLEMENTATION_JUNIT, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(ImplementationJUnit.IMPLEMENTATION_JUNIT);
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
