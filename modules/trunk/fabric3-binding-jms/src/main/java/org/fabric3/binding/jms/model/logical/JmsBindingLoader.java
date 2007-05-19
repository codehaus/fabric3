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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.model.CorrelationScheme;
import org.fabric3.binding.jms.model.CreateDestination;
import org.fabric3.binding.jms.model.DestinationDefinition;
import org.fabric3.binding.jms.model.JmsBindingMetadata;
import org.fabric3.binding.jms.model.ResponseDefinition;
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

    /**
     * Injects the registry.
     * 
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
     * @see org.fabric3.spi.loader.StAXElementLoader#load(java.lang.Object,
     *      javax.xml.stream.XMLStreamReader,
     *      org.fabric3.spi.loader.LoaderContext)
     */
    public JmsBindingDefinition load(Object configuration, XMLStreamReader reader, LoaderContext loaderContext)
        throws XMLStreamException, LoaderException {

        JmsBindingMetadata metadata = new JmsBindingMetadata();

        final String correlationScheme = reader.getAttributeValue(null, "correlationScheme");
        if (correlationScheme != null) {
            metadata.setCorrelationScheme(CorrelationScheme.valueOf(correlationScheme));
        }
        metadata.setJndiUrl(reader.getAttributeValue(null, "jndiURL"));
        metadata.setInitialContextFactory(reader.getAttributeValue(null, "initialContextFactory"));

        String name = null;
        while (true) {
            
            switch(reader.next()) {
                case START_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if ("destination".equals(name)) {
                        DestinationDefinition destination = loadDestination(reader);
                        metadata.setDestination(destination);
                    } else if ("connectionFactory".equals(name)) {
                        ConnectionFactoryDefinition connectionFactory = loadConnectionFactory(reader);
                        metadata.setConnectionFactory(connectionFactory);
                    } else if ("response".equals(name)) {
                        ResponseDefinition response = loadResponse(reader);
                        metadata.setResponse(response);
                    }
                    break;
                case END_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if("binding.jms".equals(name)) {
                        return new JmsBindingDefinition(metadata);
                    }
                    break;
            }

        }

    }

    /*
     * Loads response definition.
     */
    private ResponseDefinition loadResponse(XMLStreamReader reader) throws XMLStreamException {
        
        ResponseDefinition response = new ResponseDefinition();
        

        String name = null;
        while (true) {
            
            switch(reader.next()) {
                case START_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if ("destination".equals(name)) {
                        DestinationDefinition destination = loadDestination(reader);
                        response.setDestination(destination);
                    } else if ("connectionFactory".equals(name)) {
                        ConnectionFactoryDefinition connectionFactory = loadConnectionFactory(reader);
                        response.setConnectionFactory(connectionFactory);
                    }
                    break;
                case END_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if("response".equals(name)) {
                        return response;
                    }
                    break;
            }

        }
        
    }

    /*
     * Loads connection factory definition.
     */
    private ConnectionFactoryDefinition loadConnectionFactory(XMLStreamReader reader) {

        ConnectionFactoryDefinition connectionFactory = new ConnectionFactoryDefinition();

        connectionFactory.setName(reader.getAttributeValue(null, "name"));
        
        String create = reader.getAttributeValue(null, "create");
        if (create != null) {
            connectionFactory.setCreate(CreateDestination.valueOf(create));
        }
        
        return connectionFactory;
        
    }

    /*
     * Loads destination definition.
     */
    private DestinationDefinition loadDestination(XMLStreamReader reader) throws XMLStreamException {

        DestinationDefinition destination = new DestinationDefinition();

        destination.setName(reader.getAttributeValue(null, "name"));
        String create = reader.getAttributeValue(null, "create");
        if (create != null) {
            destination.setCreate(CreateDestination.valueOf(create));
        }
        
        return destination;
        
    }

}
