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
package org.fabric3.binding.jms.runtime.host.standalone;

import java.util.List;

import org.fabric3.api.annotation.Management;

/**
 * Management interface for the standalone pull JMS host.
 * 
 * TODO Currently this relies on the toString of Destination and assumes 
 * only one instance of the binding uses a given destination.
 *
 * @version $Revision$ $Date$
 */
@Management
public interface StandalonePullJmsHostMBean {
    
    /**
     * Gets the number of receivers for a destination.
     * @param destination String representation of the destination.
     * @return Receiver count.
     */
    int getReceiverCount(String destination);
    
    /**
     * Sets the number of receivers for a destination.
     * @param destination String representation of the destination.
     * @param receiverCount Receiver count.
     */
    void setReceiverCount(String destination, int receiverCount);
    
    /**
     * Returns the list of current destinations.
     * @return All the current destinations.
     */
    List<String> getReceivers();

}
