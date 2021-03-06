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
import javax.jms.Message;
import javax.jms.Session;

/**
 * Receives an asynchronously delivered message and optionally sends a response. Implementations support different invocation types such as
 * request-response and one-way.
 */
public interface SourceMessageListener {

    /**
     * Passes a message to the listener.
     *
     * @param request             the message passed to the listener
     * @param responseSession     the JMSSession object which is used to send response message
     * @param responseDestination JMSDestination to which the response is sent
     * @throws JmsOperationException if there is an error invoking the service bound to the JMS destination
     */
    public abstract void onMessage(Message request, Session responseSession, Destination responseDestination) throws JmsOperationException;

}