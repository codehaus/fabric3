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
package org.fabric3.binding.jms.model.logical;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.binding.jms.model.CorrelationScheme;
import org.fabric3.binding.jms.model.JmsBindingMetadata;
import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.osoa.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class JmsBindingLoader extends LoaderExtension<Object, JmsBindingDefinition> {

    /** Qualified name for the binding element. */
    private static final QName BINDING_QNAME = new QName(Constants.SCA_NS, "binding.jms");
    
    /** Nested loader cache. */
    private Map<String, NestedLoader> nestedLoaders = new HashMap<String, NestedLoader>();

    /**
     * Injects the registry.
     * @param registry Loader registry.
     */
    public JmsBindingLoader(@Reference LoaderRegistry registry) {
        super(registry);
    }

    /**
     * @see org.fabric3.extension.loader.LoaderExtension#getXMLType()
     */
    @Override
    public QName getXMLType() {
        return BINDING_QNAME;
    }

    /** 
     * @see org.fabric3.spi.loader.StAXElementLoader#load(java.lang.Object, javax.xml.stream.XMLStreamReader, org.fabric3.spi.loader.LoaderContext)
     */
    public JmsBindingDefinition load(Object configuration, XMLStreamReader reader, LoaderContext loaderContext)
        throws XMLStreamException, LoaderException {

    	JmsBindingMetadata metadata = new JmsBindingMetadata();
    	
    	final String correlationScheme = reader.getAttributeValue(null, "correlationScheme");
    	if(correlationScheme != null) {
    		metadata.setCorrelationScheme(CorrelationScheme.valueOf(correlationScheme));
    	}
    	metadata.setJndiUrl(reader.getAttributeValue(null, "jndiURL"));
    	metadata.setInitialContextFactory(reader.getAttributeValue(null, "initialContextFactory"));
    	
    	JmsBindingDefinition bindingDefinition = new JmsBindingDefinition();
    	bindingDefinition.setMetadata(metadata);
    	
    	while(reader.next() == XMLStreamConstants.START_ELEMENT) {
    		NestedLoader nestedLoader = nestedLoaders.get(reader.getName().getLocalPart());
    		if(nestedLoader != null) {
    			nestedLoader.load(metadata, reader);
    		}
    		
    	}
    	
        return bindingDefinition;

    }

}
