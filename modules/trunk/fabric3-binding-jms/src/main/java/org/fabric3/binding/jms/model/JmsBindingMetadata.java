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

import java.util.List;

import org.fabric3.spi.model.type.BindingDefinition;

/**
 * Logical model object for JMS binding definition. 
 * 
 * TODO Support for overriding request connection, response connection 
 * and operation properties from a definition document as well as 
 * activation spec and resource adaptor.
 * 
 * @version $Revision$ $Date$
 */
public class JmsBindingMetadata extends BindingDefinition {
    
    /**
     * Correlation scheme.
     */
    private CorrelationScheme correlationScheme;
    
    /**
     * JNDI initial context factory.
     */
    private String initialContextFactory;
    
    /**
     * Provider URL.
     */
    private String jndiUrl;
    
    /**
     * Destination used for receiving service requests and dispatching 
     * reference invocations.
     */
    private DestinationDefinition destination;
    
    /**
     * Connection factory definition.
     */
    private ConnectionFactoryDefinition connectionFactory;
    
    /**
     * Response definition.
     */
    private ResponseDefinition responseDefinition;
    
    /**
     * Headers.
     */
    private HeaderDefinition header;
    
    /**
     * Operation definitions.
     */
    private List<OperationPropertiesDefinition> operations;

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
     * @return the header
     */
    public HeaderDefinition getHeader() {
        return header;
    }

    /**
     * @param header the header to set
     */
    public void setHeader(HeaderDefinition header) {
        this.header = header;
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
     * @return the operations
     */
    public List<OperationPropertiesDefinition> getOperations() {
        return operations;
    }

    /**
     * @param operations the operations to set
     */
    public void setOperations(List<OperationPropertiesDefinition> operations) {
        this.operations = operations;
    }

	/**
	 * @return Definition fro sending responses.
	 */
	public ResponseDefinition getResponseDefinition() {
		return responseDefinition;
	}

	/**
	 * @param responseDefinition Definition fro sending responses.
	 */
	public void setResponseDefinition(ResponseDefinition responseDefinition) {
		this.responseDefinition = responseDefinition;
	}
    

}
