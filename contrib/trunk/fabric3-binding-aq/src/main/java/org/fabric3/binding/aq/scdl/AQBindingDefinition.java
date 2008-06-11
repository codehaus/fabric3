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
package org.fabric3.binding.aq.scdl;

import org.fabric3.binding.aq.common.InitialState;
import org.fabric3.binding.aq.introspection.AQBindingLoader;
import org.fabric3.scdl.BindingDefinition;

/**
 * Logical model object for JMS binding definition. TODO Support for overriding
 * request connection, response connection and operation properties from a
 * definition document as well as activation spec and resource adaptor.
 * 
 * @version $Revision$ $Date$
 */
public class AQBindingDefinition extends BindingDefinition {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -1068490200857726262L;
    
    private final String destinationName;
    private final InitialState initialState;
    private final int consumerCount;
    private final String dataSourceKey;
    
    /**
     * TODO DOCUMENT
     * @param destinationName
     * @param initialState
     * @param dataSourceKey
     * @param consumerCount
     */
    public AQBindingDefinition(String destinationName, InitialState initialState, String dataSourceKey, int consumerCount) {
        super(AQBindingLoader.BINDING_QNAME);
        this.destinationName = destinationName;
        this.initialState = initialState;
        this.dataSourceKey = dataSourceKey;
        this.consumerCount = consumerCount;
    }

    /**
     * TODO DOCUMENT
     * @return
     */
    public String getDestinationName() {
        return destinationName;
    }
    
    /**
     * TODO DOCUMENT
     * @return
     */
    public InitialState getInitialState() {
        return initialState;
    }
    
    /**
     * TODO DOCUMENT
     * @return
     */
    public int getConsumerCount() {
        return consumerCount;
    }
    
    /**
     * TODO DOCUMENT
     * @return
     */
    public String getDataSourceKey() {
        return dataSourceKey;
    }

}
