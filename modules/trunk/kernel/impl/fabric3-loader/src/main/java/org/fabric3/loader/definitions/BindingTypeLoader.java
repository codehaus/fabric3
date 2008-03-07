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

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.scdl.definitions.BindingType;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderRegistry;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.spi.util.stax.StaxUtil;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * Loader for definitions.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class BindingTypeLoader implements TypeLoader<BindingType> {

    /**
     * Registers the loader with the registry.
     * @param registry Injected registry
     */
    public BindingTypeLoader(@Reference LoaderRegistry registry) {
        registry.registerLoader(DefinitionsLoader.BINDING_TYPE, this);
    }

    public BindingType load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException, LoaderException {
        

        String name = reader.getAttributeValue(null, "name");
        QName qName = new QName(context.getTargetNamespace(), name);
        
        Set<QName> alwaysProvides = StaxUtil.parseListOfQNames(reader, "alwaysProvides");
        Set<QName> mayProvide = StaxUtil.parseListOfQNames(reader, "mayProvide");
        
        LoaderUtil.skipToEndElement(reader);
        
        return new BindingType(qName, alwaysProvides, mayProvide);

        
    }

}
