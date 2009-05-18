/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.binding.jms.runtime;

import java.net.URI;

import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;

/**
 * Provisions listeners with the underlying JMS infrastructure.
 *
 * @version $Revision$ $Date$
 */
public interface JmsHost {

    /**
     * Returns true if a listener for the service URI is registered.
     *
     * @param serviceUri the service URI
     * @return true if a listener is registered
     */
    boolean isRegistered(URI serviceUri);

    /**
     * Register a ResponseMessageListener which handle inbound message and send response.
     *
     * @param requestJMSObjectFactory  Factory for creating JMS objects for request.
     * @param responseJMSObjectFactory Factory for creating JMS objects for response.
     * @param messageListener          Message listener.
     * @param transactionType          Transaction type.
     * @param transactionHandler       Transaction handler.
     * @param cl                       Classloader to use.
     * @param serviceUri               URI of the service to which the binding is attached.
     */
    void registerResponseListener(JMSObjectFactory requestJMSObjectFactory,
                                  JMSObjectFactory responseJMSObjectFactory,
                                  ServiceMessageListener messageListener,
                                  TransactionType transactionType,
                                  TransactionHandler transactionHandler,
                                  ClassLoader cl,
                                  URI serviceUri);

    /**
     * Unregister the message listener at the endpoint at serviceUri
     *
     * @param serviceUri URI of the service to which the binding is attached.
     */
    void unregisterListener(URI serviceUri);
}
