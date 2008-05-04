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
package org.fabric3.binding.jms.runtime.host;

import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.JMSObjectFactory;
import org.fabric3.binding.jms.runtime.ResponseMessageListener;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;

/**
 * @version $Revision$ $Date$
 */
public interface JmsHost {

    /**
     * Register a <code>ResponseMessageListener<code> which handle inbound message and send response.
     * 
     * @param requestJMSObjectFactory Factory for creating JMS objects for request.
     * @param responseJMSObjectFactory Factory for creating JMS objects for response.
     * @param messageListener Message listener.
     * @param transactionType Transaction type.
     * @param transactionHandler Transaction handler.
     * @param cl Classloader to use.
     */
    public void registerResponseListener(JMSObjectFactory requestJMSObjectFactory,
                                         JMSObjectFactory responseJMSObjectFactory,
                                         ResponseMessageListener messageListener,
                                         TransactionType transactionType,
                                         TransactionHandler transactionHandler,
                                         ClassLoader cl);

}
