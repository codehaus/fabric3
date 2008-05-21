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
package org.fabric3.binding.aq.host;

import java.net.URI;

import javax.jms.Destination;
import javax.jms.MessageListener;
import javax.jms.XAQueueConnectionFactory;

import org.fabric3.binding.aq.tx.TransactionHandler;

/**
 * @version $Revision$ $Date$
 */
public interface AQHost {
    
    
    /**
     * Register Listeners to start consuming messages
     * @param connectionFactory
     * @param destination
     * @param listener
     * @param transactionHandler
     * @param classloader
     */
    void registerListener(XAQueueConnectionFactory connectionFactory,
                          Destination destination,                                              
                          MessageListener listener,
                          TransactionHandler transactionHandler,
                          ClassLoader classloader,
                          URI namespace);

}
