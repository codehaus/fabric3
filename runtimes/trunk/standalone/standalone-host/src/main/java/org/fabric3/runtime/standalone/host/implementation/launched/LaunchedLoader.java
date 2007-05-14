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
package org.fabric3.runtime.standalone.host.implementation.launched;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.Constants;

/**
 * @version $Rev$ $Date$
 */
public class LaunchedLoader extends LoaderExtension<Object, Launched> {
    private static final QName LAUNCHED = new QName(Constants.FABRIC3_NS, "launched");

    public LaunchedLoader(@Reference LoaderRegistry registry) {
        super(registry);
    }

    public QName getXMLType() {
        return LAUNCHED;
    }

    public Launched load(Object modelType, XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {
        String className = reader.getAttributeValue(null, "class");
        String factoryName = reader.getAttributeValue(null, "factory");
        LoaderUtil.skipToEndElement(reader);
        
        Launched impl = new Launched(className, factoryName);
        registry.loadComponentType(impl, loaderContext);
        return impl;
    }
}
