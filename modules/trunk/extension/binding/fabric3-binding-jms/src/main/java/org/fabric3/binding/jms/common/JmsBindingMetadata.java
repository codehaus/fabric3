/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.binding.jms.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;

import org.fabric3.model.type.ModelObject;

/**
 * Logical model object for JMS binding definition. TODO Support for overriding request connection, response connection and operation properties from
 * a definition document as well as activation spec and resource adaptor.
 *
 * @version $Revision$ $Date$
 */
public class JmsBindingMetadata extends ModelObject {
    private static final long serialVersionUID = 4623441503097788831L;

    /**
     * Correlation scheme.
     */
    private CorrelationScheme correlationScheme = CorrelationScheme.RequestMsgIDToCorrelID;

    /**
     * JNDI initial context factory.
     */
    private String initialContextFactory;

    /**
     * Provider URL.
     */
    private String jndiUrl;

    /**
     * Destination used for receiving service requests and dispatching reference invocations.
     */
    private DestinationDefinition destination;

    /**
     * Connection factory definition.
     */
    private ConnectionFactoryDefinition connectionFactory;

    /**
     * Response definition.
     */
    private ResponseDefinition response;

    /**
     * Header properties
     */
    private HeadersDefinition headers;

    /**
     * operation properties
     */
    private Map<String, OperationPropertiesDefinition> operationProperties;

    /**
     * @return the connectionFactory
     */
    public ConnectionFactoryDefinition getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * @param connectionFactory the connectionFactory to set
     */
    public void setConnectionFactory(ConnectionFactoryDefinition connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * @return the correlationScheme
     */
    public CorrelationScheme getCorrelationScheme() {
        return correlationScheme;
    }

    /**
     * @param correlationScheme the correlationScheme to set
     */
    public void setCorrelationScheme(CorrelationScheme correlationScheme) {
        this.correlationScheme = correlationScheme;
    }

    /**
     * @return the destination
     */
    public DestinationDefinition getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(DestinationDefinition destination) {
        this.destination = destination;
    }

    /**
     * @return the initialContextFactory
     */
    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    /**
     * @param initialContextFactory the initialContextFactory to set
     */
    public void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
    }

    /**
     * @return the jndiUrl
     */
    public String getJndiUrl() {
        return jndiUrl;
    }

    /**
     * @param jndiUrl the jndiUrl to set
     */
    public void setJndiUrl(String jndiUrl) {
        this.jndiUrl = jndiUrl;
    }

    /**
     * @return Definition for sending responses.
     */
    public ResponseDefinition getResponse() {
        return response;
    }

    /**
     * @param response Definition fro sending responses.
     */
    public void setResponse(ResponseDefinition response) {
        this.response = response;
    }

    /**
     * @return Response destination definition.
     */
    public DestinationDefinition getResponseDestination() {
        return response.getDestination();
    }

    /**
     * @return Response destination definition.
     */
    public ConnectionFactoryDefinition getResponseConnectionFactory() {
        return response.getConnectionFactory();
    }

    /**
     * @return The JNDI environment to use.
     */
    public Hashtable<String, String> getEnv() {

        Hashtable<String, String> props = new Hashtable<String, String>();

        if (jndiUrl != null) {
            props.put(Context.PROVIDER_URL, getJndiUrl());
        }
        if (initialContextFactory != null) {
            props.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());
        }

        return props;

    }

    public HeadersDefinition getHeaders() {
        return headers;
    }

    public void setHeaders(HeadersDefinition headers) {
        this.headers = headers;
    }

    public Map<String, OperationPropertiesDefinition> getOperationProperties() {
        if (operationProperties == null) {
            return Collections.emptyMap();
        } else {
            return operationProperties;
        }
    }

    public void addOperationProperties(String name, OperationPropertiesDefinition operationProperties) {
        if (this.operationProperties == null) {
            this.operationProperties = new HashMap<String, OperationPropertiesDefinition>();
        }
        this.operationProperties.put(name, operationProperties);
    }

    /**
     * Returns if the binding is configured with a response destination.�
     *
     * @return true if the binding is configured with a response destination
     */
    public boolean isResponse() {
        return response != null;
    }

}
