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

package org.fabric3.binding.jms.wire.helper;

import javax.jms.ConnectionFactory;

import org.fabric3.binding.jms.model.ConnectionFactoryDefinition;

/**
 * Helper class for looking up connection factories.
 * 
 * @version $Revison$ $Date$
 */
public class ConnectionFactoryHelper extends AdministeredObjectHelper {
    
    /**
     * Utility class constructor.
     */
    private ConnectionFactoryHelper() {
    }
    
    /**
     * Gets a connection factory based on the rules defined by the SCA JMS bidnding rules.
     * 
     * @param definition Destination definition.
     * @param providerUrl Provider Url.
     * @param ctxFactory Initial context factory.
     * @return JMS destination.
     */
    public static ConnectionFactory getConnectionFactory(ConnectionFactoryDefinition definition, String providerUrl, String ctxFactory) {
        
        switch(definition.getCreate()) {
            case ifnotexist:
            case never:
                return  (ConnectionFactory) lookup(definition.getName(), providerUrl, ctxFactory);
            case always:
                throw new UnsupportedOperationException("Not supported yet");
            default:
                throw new IllegalArgumentException("Unexpected create option");
        }
        
    }

}
