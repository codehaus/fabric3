/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * Dispatches an asynchronously received message to a service. Implementations support request-response and one-way operations. For request-response
 * operations, responses will be enqueued using the response session and destination.
 */
public interface ServiceMessageListener {

    /**
     * Dispatch a received message to a service.
     *
     * @param request             the message passed to the listener
     * @param responseSession     the JMSSession object which is used to send response message or null if the operation is one-way
     * @param responseDestination JMSDestination to which the response is sent or null if the operation is one-way
     * @throws JmsServiceException    thrown if the service throws an exception. For request-response operations, the exception cause will be sent as
     *                                a fault response prior to it being thrown.
     * @throws JmsBadMessageException if a message is received that cannot be processed and should be redelivered
     * @throws JMSException
     */
    public abstract void onMessage(Message request, Session responseSession, Destination responseDestination)
            throws JmsServiceException, JmsBadMessageException, JMSException;

}