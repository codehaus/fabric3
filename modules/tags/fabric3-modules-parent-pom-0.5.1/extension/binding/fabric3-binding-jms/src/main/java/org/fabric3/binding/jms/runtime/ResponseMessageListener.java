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
package org.fabric3.binding.jms.runtime;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Session;

/**
 * A <CODE>ResponseMessageListener</CODE> object is used to receive
 * asynchronously delivered messages and then optionally send response
 */
public interface ResponseMessageListener {

    /**
     * Passes a message to the listener.
     *
     * @param request the message passed to the listener
     * @param responseSession the JMSSession object which is used to send response message
     * @param responseDestination JMSDestination to which the response is sent
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public abstract void onMessage(Message request, Session responseSession, Destination responseDestination);

}