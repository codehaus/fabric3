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
package org.fabric3.binding.jms.introspection;

import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.common.CorrelationScheme;
import org.fabric3.binding.jms.common.CreateOption;
import org.fabric3.binding.jms.common.DestinationDefinition;
import org.fabric3.binding.jms.common.DestinationType;
import org.fabric3.binding.jms.common.HeadersDefinition;
import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.common.JmsURIMetadata;
import org.fabric3.binding.jms.common.OperationPropertiesDefinition;
import org.fabric3.binding.jms.common.PropertyAwareObject;
import org.fabric3.binding.jms.common.ResponseDefinition;
import org.fabric3.binding.jms.scdl.JmsBindingDefinition;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidValue;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.TypeLoader;


/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class JmsBindingLoader implements TypeLoader<JmsBindingDefinition> {

    /**
     * Qualified name for the binding element.
     */
    public static final QName BINDING_QNAME = new QName(Constants.SCA_NS, "binding.jms");

    private final LoaderHelper loaderHelper;

    /**
     * Constructor.
     *
     * @param loaderHelper the loaderHelper
     */
    public JmsBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
    }

    public JmsBindingDefinition load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {

        JmsBindingMetadata metadata = null;
        String uri = reader.getAttributeValue(null, "uri");
        JmsBindingDefinition bd;
        if (uri != null) {
            JmsURIMetadata uriMeta;
            try {
                uriMeta = JmsURIMetadata.parseURI(uri);
                metadata = JmsLoaderHelper.getJmsMetadataFromURI(uriMeta);
            } catch (URISyntaxException e) {
                InvalidValue failure = new InvalidValue("Invalid JMS binding URI: " + uri, uri, reader, e);
                introspectionContext.addError(failure);
            }
            bd = new JmsBindingDefinition(loaderHelper.getURI(uri), metadata);
        } else {
            metadata = new JmsBindingMetadata();
            bd = new JmsBindingDefinition(metadata);
        }
        final String correlationScheme = reader.getAttributeValue(null, "correlationScheme");
        if (correlationScheme != null) {
            metadata.setCorrelationScheme(CorrelationScheme.valueOf(correlationScheme));
        }
        metadata.setJndiUrl(reader.getAttributeValue(null, "jndiURL"));
        metadata.setInitialContextFactory(reader.getAttributeValue(null, "initialContextFactory"));
        loaderHelper.loadPolicySetsAndIntents(bd, reader, introspectionContext);
        if (uri != null) {
            while (true) {
                if (END_ELEMENT == reader.next() && "binding.jms".equals(reader.getName().getLocalPart())) {
                    return bd;
                }
            }
        }
        String name;
        while (true) {

            switch (reader.next()) {
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
                } else if ("headers".equals(name)) {
                    HeadersDefinition headers = loadHeaders(reader, introspectionContext);
                    metadata.setHeaders(headers);
                } else if ("operationProperties".equals(name)) {
                    OperationPropertiesDefinition operationProperties = loadOperationProperties(reader, introspectionContext);
                    metadata.addOperationProperties(operationProperties.getName(), operationProperties);
                }
                break;
            case END_ELEMENT:
                name = reader.getName().getLocalPart();
                if ("binding.jms".equals(name)) {
                    bd.setGeneratedTargetUri(loaderHelper.getURI(JmsLoaderHelper.generateURI(metadata)));
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

        String name;
        while (true) {

            switch (reader.next()) {
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
                if ("response".equals(name)) {
                    return response;
                }
                break;
            }

        }

    }

    /*
     * Loads connection factory definition.
     */
    private ConnectionFactoryDefinition loadConnectionFactory(XMLStreamReader reader) throws XMLStreamException {

        ConnectionFactoryDefinition connectionFactory = new ConnectionFactoryDefinition();

        connectionFactory.setName(reader.getAttributeValue(null, "name"));

        String create = reader.getAttributeValue(null, "create");
        if (create != null) {
            connectionFactory.setCreate(CreateOption.valueOf(create));
        }
        loadProperties(reader, connectionFactory, "connectionFactory");

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
            destination.setCreate(CreateOption.valueOf(create));
        }

        String type = reader.getAttributeValue(null, "type");
        if (type != null) {
            destination.setDestinationType(DestinationType.valueOf(type));
        }

        loadProperties(reader, destination, "destination");

        return destination;

    }

    /*
     * Loads headers.
     */
    private HeadersDefinition loadHeaders(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        HeadersDefinition headers = new HeadersDefinition();
        headers.setJMSCorrelationId(reader.getAttributeValue(null, "JMSCorrelationId"));
        String deliveryMode = reader.getAttributeValue(null, "JMSDeliveryMode");
        if (deliveryMode != null) {
            try {
                headers.setJMSDeliveryMode(Integer.valueOf(deliveryMode));
            } catch (NumberFormatException nfe) {
                InvalidValue failure =
                        new InvalidValue(deliveryMode + " is not a legal int value for JMSDeliveryMode", deliveryMode, reader, nfe);
                introspectionContext.addError(failure);
            }
        }
        String priority = reader.getAttributeValue(null, "JMSPriority");
        if (priority != null) {
            try {
                headers.setJMSPriority(Integer.valueOf(priority));
            } catch (NumberFormatException nfe) {
                InvalidValue failure =
                        new InvalidValue(priority + " is not a legal int value for JMSPriority", priority, reader, nfe);
                introspectionContext.addError(failure);
            }
        }
        String timeToLive = reader.getAttributeValue(null, "JMSTimeToLive");
        if (timeToLive != null) {
            try {
                headers.setJMSTimeToLive(Long.valueOf(timeToLive));
            } catch (NumberFormatException nfe) {
                InvalidValue failure =
                        new InvalidValue(timeToLive + " is not a legal int value for JMSTimeToLive", timeToLive, reader, nfe);
                introspectionContext.addError(failure);
            }
        }
        headers.setJMSType(reader.getAttributeValue(null, "JMSType"));
        loadProperties(reader, headers, "headers");
        return headers;
    }

    /*
     * Loads operation properties.
     */
    private OperationPropertiesDefinition loadOperationProperties(XMLStreamReader reader, IntrospectionContext introspectionContext)
            throws XMLStreamException {
        OperationPropertiesDefinition optProperties = new OperationPropertiesDefinition();
        optProperties.setName(reader.getAttributeValue(null, "name"));
        optProperties.setNativeOperation(reader.getAttributeValue(null, "nativeOperation"));
        String name;
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                name = reader.getName().getLocalPart();
                if ("headers".equals(name)) {
                    HeadersDefinition headersDefinition = loadHeaders(reader, introspectionContext);
                    optProperties.setHeaders(headersDefinition);
                } else if ("property".equals(name)) {
                    loadProperty(reader, optProperties);
                }
                break;
            case END_ELEMENT:
                name = reader.getName().getLocalPart();
                if ("operationProperties".equals(name)) {
                    return optProperties;
                }
                break;
            }
        }

    }

    /*
    * Loads properties.
    */
    private void loadProperties(XMLStreamReader reader, PropertyAwareObject parent, String parentName) throws XMLStreamException {
        String name;
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                name = reader.getName().getLocalPart();
                if ("property".equals(name)) {
                    loadProperty(reader, parent);
                }
                break;
            case END_ELEMENT:
                name = reader.getName().getLocalPart();
                if (parentName.equals(name)) {
                    return;
                }
                break;
            }
        }
    }

    /**
     * Loads a property. TODO Support property type.
     */
    private void loadProperty(XMLStreamReader reader, PropertyAwareObject parent) throws XMLStreamException {
        final String key = reader.getAttributeValue(null, "name");
        final String value = reader.getElementText();
        parent.addProperty(key, value);
    }


}
