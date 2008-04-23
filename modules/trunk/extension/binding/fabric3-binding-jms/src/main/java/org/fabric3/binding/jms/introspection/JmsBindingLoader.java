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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.Constants;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.common.AdministeredObjectDefinition;
import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.common.CorrelationScheme;
import org.fabric3.binding.jms.common.CreateOption;
import org.fabric3.binding.jms.common.DestinationDefinition;
import org.fabric3.binding.jms.common.DestinationType;
import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.common.JmsURIMetadata;
import org.fabric3.binding.jms.common.ResponseDefinition;
import org.fabric3.binding.jms.scdl.JmsBindingDefinition;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderRegistry;
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
    public static final QName BINDING_QNAME = new QName(Constants.SCA_NS,
                                                        "binding.jms");

    private LoaderRegistry registry;
    private final LoaderHelper loaderHelper;

    /**
     * Constructor.
     *
     * @param registry     Loader registry.
     * @param loaderHelper the loaderHelper
     */
    public JmsBindingLoader(@Reference
    LoaderRegistry registry, @Reference
    LoaderHelper loaderHelper) {
        this.registry = registry;
        this.loaderHelper = loaderHelper;
    }

    @Init
    public void start() {
        registry.registerLoader(BINDING_QNAME, this);
    }

    @Destroy
    public void stop() {
        registry.unregisterLoader(BINDING_QNAME);
    }

    public JmsBindingDefinition load(XMLStreamReader reader,
                                     IntrospectionContext introspectionContext)
            throws XMLStreamException, LoaderException {

        JmsBindingMetadata metadata;
        String uri = reader.getAttributeValue(null, "uri");
        JmsBindingDefinition bd;
        if (uri != null) {
            JmsURIMetadata uriMeta = JmsURIMetadata.parseURI(uri);
            metadata = JmsLoaderHelper.getJmsMetadataFromURI(uriMeta);
            bd = new JmsBindingDefinition(loaderHelper.getURI(uri), metadata);
        } else {
            metadata = new JmsBindingMetadata();
            bd = new JmsBindingDefinition(metadata);
        }
        final String correlationScheme = reader.getAttributeValue(null, "correlationScheme");
        if (correlationScheme != null) {
            metadata.setCorrelationScheme(CorrelationScheme
                    .valueOf(correlationScheme));
        }
        metadata.setJndiUrl(reader.getAttributeValue(null, "jndiURL"));
        metadata.setInitialContextFactory(reader.getAttributeValue(null, "initialContextFactory"));
        loaderHelper.loadPolicySetsAndIntents(bd, reader);
        if (uri != null) {
            while (true) {
                if (END_ELEMENT == reader.next() && "binding.jms".equals(reader.getName().getLocalPart())) {
                    return bd;
                }
            }
        }
        String name = null;
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
    * Loads properties. TODO Support property type.
    */
    private void loadProperties(XMLStreamReader reader, AdministeredObjectDefinition parent, String parentName) throws XMLStreamException {
        String name;
        while (true) {
            switch (reader.next()) {
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
                if (parentName.equals(name)) {
                    return;
                }
                break;
            }

        }

    }

}
