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


import org.fabric3.api.annotation.LogLevel;

/**
 * Monitor interface for JMS Host.
 * @version $Rev: 3137 $ $Date: 2008-03-18 02:31:06 +0800 (Tue, 18 Mar 2008) $
 */
public interface JMSRuntimeMonitor {

    /**
     * Callback when a service has been provisioned as a Hessian endpoint
     *
     * @param address the endpoint address
     */
    @LogLevel("INFO")
    void registerListener(Object destination);

    /**
     * Callback when an error happens when handle message.
     *
     * @param address the endpoint address
     */
    @LogLevel("INFO")
    void jmsListenerError(Exception address);


    /**
     * Callback indicating the extension has been stopped.
     */
    @LogLevel("INFO")
    void jmsRuntimeStop();


}
