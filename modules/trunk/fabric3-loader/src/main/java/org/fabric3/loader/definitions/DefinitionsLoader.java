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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.osoa.sca.Constants.SCA_NS;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.scdl.definitions.AbstractDefinition;
import org.fabric3.scdl.definitions.BindingType;
import org.fabric3.scdl.definitions.ImplementationType;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * Loader for definitions.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class DefinitionsLoader implements StAXElementLoader<Resource> {
    
    static final QName INTENT = new QName(SCA_NS, "intent");
    static final QName DESCRIPTION = new QName(SCA_NS, "description");
    static final QName POLICY_SET = new QName(SCA_NS, "policySet");
    static final QName BINDING_TYPE = new QName(SCA_NS, "bindingType");
    static final QName IMPLEMENTATION_TYPE = new QName(SCA_NS, "implementationType");
    
    private static final QName DEFINITIONS = new QName(SCA_NS, "definitions");
    
    private LoaderRegistry loaderRegistry;

    public DefinitionsLoader(@Reference LoaderRegistry registry) {
        this.loaderRegistry = registry;
        loaderRegistry.registerLoader(DEFINITIONS, this);
    }

    /**
     * @see org.fabric3.spi.loader.StAXElementLoader#load(javax.xml.stream.XMLStreamReader, org.fabric3.spi.loader.LoaderContext)
     */
    public Resource load(XMLStreamReader reader, LoaderContext parentContext) throws XMLStreamException, LoaderException {

        Resource resource = new Resource();
        
        String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
        
        LoaderContext context = new LoaderContextImpl(parentContext, targetNamespace);
        
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                QName qname = reader.getName();
                AbstractDefinition definition = null;
                if (INTENT.equals(qname)) {
                    definition = loaderRegistry.load(reader, Intent.class, context);
                } else if (POLICY_SET.equals(qname)) {
                    definition = loaderRegistry.load(reader, PolicySet.class, context);
                } else if (BINDING_TYPE.equals(qname)) {
                    definition = loaderRegistry.load(reader, BindingType.class, context);
                } else if (IMPLEMENTATION_TYPE.equals(qname)) {
                    definition = loaderRegistry.load(reader, ImplementationType.class, context);
                }
                if(definition != null) {
                    QNameSymbol symbol = new QNameSymbol(definition.getName());
                    ResourceElement<QNameSymbol, AbstractDefinition> element = 
                        new ResourceElement<QNameSymbol, AbstractDefinition>(symbol, definition);
                    resource.addResourceElement(element);
                }
                break;
            case END_ELEMENT:
                assert DEFINITIONS.equals(reader.getName());
                return resource;
            }
        }
        
    }

}
