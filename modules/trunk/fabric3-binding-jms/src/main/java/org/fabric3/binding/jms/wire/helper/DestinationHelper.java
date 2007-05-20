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

import javax.jms.Destination;

import org.fabric3.binding.jms.model.DestinationDefinition;

/**
 * Helper class for lookingp up destinations.
 * 
 * @version $Revison$ $Date$
 */
public class DestinationHelper extends AdministeredObjectHelper {
    
    /**
     * Utility class constructor.
     */
    private DestinationHelper() {
    }
    
    /**
     * Gets a destination based on the rules defined by the SCA JMS bidnding rules.
     * 
     * @param definition Destination definition.
     * @param providerUrl Provider Url.
     * @param ctxFactory Initial context factory.
     * @return JMS destination.
     */
    public static Destination getDestination(DestinationDefinition definition, String providerUrl, String ctxFactory) {
        
        switch(definition.getCreate()) {
            case ifnotexist:
            case never:
                return  (Destination) lookup(definition.getName(), providerUrl, ctxFactory);
            case always:
                throw new UnsupportedOperationException("Not supported yet");
            default:
                throw new IllegalArgumentException("Unexpected create option");
        }
        
    }

}
