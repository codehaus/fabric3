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
package org.fabric3.binding.aq.model.logical;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.sql.SQLException;

import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import oracle.jdbc.pool.OracleDataSource;

import org.enhydra.jdbc.standard.StandardXADataSource;
import org.fabric3.binding.aq.model.AQBindingMetadata;
import org.fabric3.binding.aq.model.AdministeredObjectDefinition;
import org.fabric3.binding.aq.model.CorrelationScheme;
import org.fabric3.binding.aq.model.CreateOption;
import org.fabric3.binding.aq.model.DestinationDefinition;
import org.fabric3.binding.aq.model.DestinationType;
import org.fabric3.binding.aq.model.ResponseDefinition;
import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.PolicyHelper;
import org.osoa.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class AQBindingLoader extends LoaderExtension<AQBindingDefinition> {

    /** Qualified name for the binding element. */
    public static final QName BINDING_QNAME = new QName(Constants.SCA_NS, "binding.aq");

    /** Assiste with Policies */
    private final PolicyHelper policyHelper;    
              

    /**
     * Injects the registry.
     *
     * @param registry Loader registry.
     */
    public AQBindingLoader(@Reference LoaderRegistry registry, @Reference PolicyHelper policyHelper) {
        super(registry);
        this.policyHelper = policyHelper;           
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
    public AQBindingDefinition load(final XMLStreamReader reader, final IntrospectionContext loaderContext)
        throws XMLStreamException, LoaderException {             
        
        final AQBindingMetadata metadata = new AQBindingMetadata();
        final AQBindingDefinition bd = new AQBindingDefinition(metadata);               

        final String correlationScheme = reader.getAttributeValue(null, "correlationScheme");
        if (correlationScheme != null) {
            metadata.setCorrelationScheme(CorrelationScheme.valueOf(correlationScheme));
        }        

        policyHelper.loadPolicySetsAndIntents(bd, reader);

        String name = null;
        while (true) {

            switch(reader.next()) {
                case START_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if ("destination".equals(name)) {
                        DestinationDefinition destination = loadDestination(reader);
                        metadata.setDestination(destination);
                    } else if ("response".equals(name)) {
                        ResponseDefinition response = loadResponse(reader);
                        metadata.setResponse(response);
                    }
                    break;
                case END_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if("binding.aq".equals(name)) {
                        return bd;
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
     * Loads destination definition.
     */
    private DestinationDefinition loadDestination(XMLStreamReader reader) throws XMLStreamException {

        DestinationDefinition destination = new DestinationDefinition();

        destination.setName(reader.getAttributeValue(null, "name"));

        String create = reader.getAttributeValue(null, "create");
        if (create != null) {
            destination.setCreate(CreateOption.valueOf(create));
        }

        String type = reader.getAttributeValue(null, "type");
        if(type != null) {
            destination.setDestinationType(DestinationType.valueOf(type));
        }

        loadProperties(reader, destination, "destination");

        return destination;

    }

    /*
     * Loads properties. TODO Support property type.
     */
    private void loadProperties(XMLStreamReader reader, AdministeredObjectDefinition parent, String parentName) throws XMLStreamException {

        String name = null;
        while (true) {
            switch(reader.next()) {
                case START_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if ("property".equals(name)) {
                        final String key = reader.getAttributeValue(null, "name");
                        final String value = reader.getElementText();
                        parent.addProperty(key, value);
                    }
                    break;
                case END_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if(parentName.equals(name)) {
                        return;
                    }
                    break;
            }
        }
    }    
}
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
package org.fabric3.binding.aq.model.logical;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.sql.SQLException;

import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import oracle.jdbc.pool.OracleDataSource;

import org.enhydra.jdbc.standard.StandardXADataSource;
import org.fabric3.binding.aq.model.AQBindingMetadata;
import org.fabric3.binding.aq.model.AdministeredObjectDefinition;
import org.fabric3.binding.aq.model.CorrelationScheme;
import org.fabric3.binding.aq.model.CreateOption;
import org.fabric3.binding.aq.model.DestinationDefinition;
import org.fabric3.binding.aq.model.DestinationType;
import org.fabric3.binding.aq.model.ResponseDefinition;
import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.PolicyHelper;
import org.osoa.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class AQBindingLoader extends LoaderExtension<AQBindingDefinition> {

    /** Qualified name for the binding element. */
    public static final QName BINDING_QNAME = new QName(Constants.SCA_NS, "binding.aq");

    /** Assiste with Policies */
    private final PolicyHelper policyHelper;    
              

    /**
     * Injects the registry.
     *
     * @param registry Loader registry.
     */
    public AQBindingLoader(@Reference LoaderRegistry registry, @Reference PolicyHelper policyHelper) {
        super(registry);
        this.policyHelper = policyHelper;           
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
    public AQBindingDefinition load(final XMLStreamReader reader, final IntrospectionContext loaderContext)
        throws XMLStreamException, LoaderException {             
        
        final AQBindingMetadata metadata = new AQBindingMetadata();
        final AQBindingDefinition bd = new AQBindingDefinition(metadata);               

        final String correlationScheme = reader.getAttributeValue(null, "correlationScheme");
        if (correlationScheme != null) {
            metadata.setCorrelationScheme(CorrelationScheme.valueOf(correlationScheme));
        }        

        policyHelper.loadPolicySetsAndIntents(bd, reader);

        String name = null;
        while (true) {

            switch(reader.next()) {
                case START_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if ("destination".equals(name)) {
                        DestinationDefinition destination = loadDestination(reader);
                        metadata.setDestination(destination);
                    } else if ("response".equals(name)) {
                        ResponseDefinition response = loadResponse(reader);
                        metadata.setResponse(response);
                    }
                    break;
                case END_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if("binding.aq".equals(name)) {
                        return bd;
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
     * Loads destination definition.
     */
    private DestinationDefinition loadDestination(XMLStreamReader reader) throws XMLStreamException {

        DestinationDefinition destination = new DestinationDefinition();

        destination.setName(reader.getAttributeValue(null, "name"));

        String create = reader.getAttributeValue(null, "create");
        if (create != null) {
            destination.setCreate(CreateOption.valueOf(create));
        }

        String type = reader.getAttributeValue(null, "type");
        if(type != null) {
            destination.setDestinationType(DestinationType.valueOf(type));
        }

        loadProperties(reader, destination, "destination");

        return destination;

    }

    /*
     * Loads properties. TODO Support property type.
     */
    private void loadProperties(XMLStreamReader reader, AdministeredObjectDefinition parent, String parentName) throws XMLStreamException {

        String name = null;
        while (true) {
            switch(reader.next()) {
                case START_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if ("property".equals(name)) {
                        final String key = reader.getAttributeValue(null, "name");
                        final String value = reader.getElementText();
                        parent.addProperty(key, value);
                    }
                    break;
                case END_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if(parentName.equals(name)) {
                        return;
                    }
                    break;
            }
        }
    }    
}
