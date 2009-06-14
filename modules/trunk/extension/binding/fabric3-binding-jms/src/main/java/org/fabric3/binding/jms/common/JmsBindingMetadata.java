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
     * Connection factory definition. Set for the default.
     */
    private ConnectionFactoryDefinition connectionFactory = new ConnectionFactoryDefinition();

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
     * Returns if the binding is configured with a response destination.Ê
     *
     * @return true if the binding is configured with a response destination
     */
    public boolean isResponse() {
        return response != null;
    }

}
