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

import java.util.Map;

import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.common.CreateOption;
import org.fabric3.binding.jms.common.DestinationDefinition;
import org.fabric3.binding.jms.common.DestinationType;
import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.common.JmsURIMetadata;
import org.fabric3.binding.jms.common.ResponseDefinition;

/**
 * Helper class for JMS loader.
 */
public class JmsLoaderHelper {
    private static final String DEFAULT_CLIENT_QUEUE = "clientQueue";
    /**
     * Jndi name for default ConnectionFactory
     */
    private static final String DEFAULT_JMS_CONNECTION_FACTORY = "connectionFactory";

    private JmsLoaderHelper() {
    }

    /**
     * Transform a JmsURIMetadata object to a JmsBindingMetadata.
     *
     * @param uriMeta JmsURIMetadata
     * @return a equivalent JmsURIMetadata object
     */
    static JmsBindingMetadata getJmsMetadataFromURI(JmsURIMetadata uriMeta) {
        JmsBindingMetadata result = new JmsBindingMetadata();
        Map<String, String> uriProperties = uriMeta.getProperties();

        // Destination
        DestinationDefinition destination = new DestinationDefinition();
        String destinationType = uriProperties.get(JmsURIMetadata.DESTINATIONTYPE);
        if ("topic".equalsIgnoreCase(destinationType)) {
            destination.setDestinationType(DestinationType.topic);
        }
        destination.setName(uriMeta.getDestination());
        destination.setCreate(CreateOption.never); // always assume the destination already exists
        result.setDestination(destination);

        // ConnectionFactory
        ConnectionFactoryDefinition connectionFactory = new ConnectionFactoryDefinition();
        String connectionFactoryName = uriProperties
                .get(JmsURIMetadata.CONNECTIONFACORYNAME);
        if (connectionFactoryName == null) {
            connectionFactory.setName(DEFAULT_JMS_CONNECTION_FACTORY);
        } else {
            connectionFactory.setName(connectionFactoryName);
        }
        connectionFactory.setCreate(CreateOption.never);
        result.setConnectionFactory(connectionFactory);

        // Response copy configuration of request
        ResponseDefinition response = new ResponseDefinition();
        response.setConnectionFactory(connectionFactory);
        DestinationDefinition responseDestinationDef = new DestinationDefinition();
        String responseDestination = uriProperties.get(JmsURIMetadata.RESPONSEDESTINAT);
        if (responseDestination != null) {
            responseDestinationDef.setName(responseDestination);
        } else {
            responseDestinationDef.setName(DEFAULT_CLIENT_QUEUE);

        }
        responseDestinationDef.setCreate(CreateOption.never);
        response.setDestination(responseDestinationDef);
        result.setResponse(response);
        return result;
    }

    /**
     * Generate an URI from JmsBindingMetadata. This may be removed when call eliminate dependency on binding's URI.
     */
    static String generateURI(JmsBindingMetadata metadata) {
        StringBuilder builder = new StringBuilder();
        builder.append("jms:").append(metadata.getDestination().getName())
                .append("?connectionFactory=").append(
                metadata.getConnectionFactory().getName());
        return builder.toString();
    }
}
