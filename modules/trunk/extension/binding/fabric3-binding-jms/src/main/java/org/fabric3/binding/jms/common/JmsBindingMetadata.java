/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 * Encapsulates binding configuration.
 * <p/>
 * TODO Support for overriding request connection, response connection and operation properties from an activation spec and resource adaptor.
 *
 * @version $Revision$ $Date$
 */
public class JmsBindingMetadata extends ModelObject {
    private static final long serialVersionUID = 4623441503097788831L;

    private CorrelationScheme correlationScheme = CorrelationScheme.RequestMsgIDToCorrelID;
    private String initialContextFactory;
    private String jndiUrl;
    private DestinationDefinition destination;
    private ConnectionFactoryDefinition connectionFactory = new ConnectionFactoryDefinition();
    private ResponseDefinition response;
    private HeadersDefinition headers;
    private Map<String, OperationPropertiesDefinition> operationProperties;

    public ConnectionFactoryDefinition getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactoryDefinition connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public CorrelationScheme getCorrelationScheme() {
        return correlationScheme;
    }

    public void setCorrelationScheme(CorrelationScheme correlationScheme) {
        this.correlationScheme = correlationScheme;
    }

    public DestinationDefinition getDestination() {
        return destination;
    }

    public void setDestination(DestinationDefinition destination) {
        this.destination = destination;
    }

    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    public void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
    }

    public String getJndiUrl() {
        return jndiUrl;
    }

    public void setJndiUrl(String jndiUrl) {
        this.jndiUrl = jndiUrl;
    }

    public ResponseDefinition getResponse() {
        return response;
    }

    public void setResponse(ResponseDefinition response) {
        this.response = response;
    }

    public DestinationDefinition getResponseDestination() {
        return response.getDestination();
    }

    public ConnectionFactoryDefinition getResponseConnectionFactory() {
        return response.getConnectionFactory();
    }

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

    public boolean isResponse() {
        return response != null;
    }

}
