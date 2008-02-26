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
package org.fabric3.binding.aq.model;

import java.util.Hashtable;

import javax.naming.Context;
import javax.sql.DataSource;

import org.enhydra.jdbc.standard.StandardXADataSource;

/**
 * Logical model object for JMS binding definition. TODO Support for overriding
 * request connection, response connection and operation properties from a
 * definition document as well as activation spec and resource adaptor.
 * 
 * @version $Revision$ $Date: 2007-09-15 13:05:03 +0100 (Sat, 15 Sep
 *          2007) $
 */
public class AQBindingMetadata {

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
     * Response definition.
     */
    private ResponseDefinition response;

    /**
     * 
     * @return
     */
    private DataSource dataSource;

    /**
     * @return Returns the xaDataSource.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param xaDataSource
     *            The xaDataSource to set.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @return the correlationScheme
     */
    public CorrelationScheme getCorrelationScheme() {
        return correlationScheme;
    }

    /**
     * @param correlationScheme
     *            the correlationScheme to set
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
     * @param destination
     *            the destination to set
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
     * @param initialContextFactory
     *            the initialContextFactory to set
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
     * @param jndiUrl
     *            the jndiUrl to set
     */
    public void setJndiUrl(String jndiUrl) {
        this.jndiUrl = jndiUrl;
    }   

    /**
     * @param responseDefinition
     *            Definition fro sending responses.
     */
    public void setResponse(ResponseDefinition response) {
        this.response = response;
    }

    /**
     * @return Response destination definition.
     */
    public DestinationDefinition getResponseDestination() {
        if (response != null) {
            return response.getDestination();
        } else {
            /* TODO REFACTOR */
            return null;
        }
    }
}
