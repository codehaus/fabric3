/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.loader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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
import org.fabric3.binding.jms.model.JmsBindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;


/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class JmsBindingLoader implements TypeLoader<JmsBindingDefinition> {

    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();

    static {
        ATTRIBUTES.put("uri", "uri");
        ATTRIBUTES.put("correlationScheme", "correlationScheme");
        ATTRIBUTES.put("jndiURL", "jndiURL");
        ATTRIBUTES.put("initialContextFactory", "initialContextFactory");
        ATTRIBUTES.put("requires", "requires");
        ATTRIBUTES.put("policySets", "policySets");
        ATTRIBUTES.put("name", "name");
        ATTRIBUTES.put("create", "create");
        ATTRIBUTES.put("type", "type");
        ATTRIBUTES.put("destination", "destination");
        ATTRIBUTES.put("connectionFactory", "connectionFactory");
        ATTRIBUTES.put("JMSType", "JMSType");
        ATTRIBUTES.put("JMSTimeToLive", "JMSTimeToLive");
        ATTRIBUTES.put("JMSPriority", "JMSPriority");
        ATTRIBUTES.put("JMSDeliveryMode", "JMSDeliveryMode");
        ATTRIBUTES.put("JMSCorrelationId", "JMSCorrelationId");
        ATTRIBUTES.put("name", "name");
    }

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
        validateAttributes(reader, introspectionContext);

        JmsBindingMetadata metadata;
        String uri = reader.getAttributeValue(null, "uri");
        JmsBindingDefinition bd;
        if (uri != null) {
            JmsURIMetadata uriMeta;
            try {
                uriMeta = JmsURIMetadata.parseURI(uri);
                metadata = JmsLoaderHelper.getJmsMetadataFromURI(uriMeta);
            } catch (URISyntaxException e) {
                InvalidValue failure = new InvalidValue("Invalid JMS binding URI: " + uri, reader, e);
                introspectionContext.addError(failure);
                return null;
            }
            bd = new JmsBindingDefinition(loaderHelper.getURI(uri), metadata, loaderHelper.loadKey(reader));
        } else {
            metadata = new JmsBindingMetadata();
            bd = new JmsBindingDefinition(metadata, loaderHelper.loadKey(reader));
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
                    // needed for callbacks
                    String destination = bd.getMetadata().getDestination().getName();
                    URI bindingUri = URI.create("jms:" + destination);
                    bd.setGeneratedTargetUri(bindingUri);
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
                        new InvalidValue(deliveryMode + " is not a legal int value for JMSDeliveryMode", reader, nfe);
                introspectionContext.addError(failure);
            }
        }
        String priority = reader.getAttributeValue(null, "JMSPriority");
        if (priority != null) {
            try {
                headers.setJMSPriority(Integer.valueOf(priority));
            } catch (NumberFormatException nfe) {
                InvalidValue failure =
                        new InvalidValue(priority + " is not a legal int value for JMSPriority", reader, nfe);
                introspectionContext.addError(failure);
            }
        }
        String timeToLive = reader.getAttributeValue(null, "JMSTimeToLive");
        if (timeToLive != null) {
            try {
                headers.setJMSTimeToLive(Long.valueOf(timeToLive));
            } catch (NumberFormatException nfe) {
                InvalidValue failure =
                        new InvalidValue(timeToLive + " is not a legal int value for JMSTimeToLive", reader, nfe);
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

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!ATTRIBUTES.containsKey(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}
