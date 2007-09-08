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
package org.fabric3.binding.jms.model;

import java.util.Hashtable;

import javax.naming.Context;

/**
 * Logical model object for JMS binding definition. TODO Support for overriding
 * request connection, response connection and operation properties from a
 * definition document as well as activation spec and resource adaptor.
 * 
 * @version $Revision$ $Date$
 */
public class JmsBindingMetadata {

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
     * Destination used for receiving service requests and dispatching reference
     * invocations.
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
     * @return Definition fro sending responses.
     */
    public ResponseDefinition getResponse() {
        return response;
    }

    /**
     * @param responseDefinition Definition fro sending responses.
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
        
        props.put(Context.PROVIDER_URL, getJndiUrl());
        props.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());
        
        return props;
        
    }

}
