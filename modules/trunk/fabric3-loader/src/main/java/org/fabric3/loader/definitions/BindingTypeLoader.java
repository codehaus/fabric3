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
package org.fabric3.loader.definitions;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.scdl.definitions.BindingType;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.util.stax.StaxUtil;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * Loader for definitions.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class BindingTypeLoader implements StAXElementLoader<BindingType> {

    /**
     * Registers the loader with the registry.
     * @param registry Injected registry
     */
    public BindingTypeLoader(@Reference LoaderRegistry registry) {
        registry.registerLoader(DefinitionsLoader.BINDING_TYPE, this);
    }

    /**
     * @see org.fabric3.spi.loader.StAXElementLoader#load(javax.xml.stream.XMLStreamReader, org.fabric3.spi.loader.LoaderContext)
     */
    public BindingType load(XMLStreamReader reader, LoaderContext context) throws XMLStreamException, LoaderException {
        

        String name = reader.getAttributeValue(null, "name");
        QName qName = new QName(context.getTargetNamespace(), name);
        
        BindingType bindingType = new BindingType();
        bindingType.setName(qName);
        bindingType.setAlwaysProvide(StaxUtil.parseListOfQNames(reader, "alwaysProvides"));
        bindingType.setAlwaysProvide(StaxUtil.parseListOfQNames(reader, "mayProvide"));
        
        LoaderUtil.skipToEndElement(reader);
        
       return bindingType;

        
    }

}
