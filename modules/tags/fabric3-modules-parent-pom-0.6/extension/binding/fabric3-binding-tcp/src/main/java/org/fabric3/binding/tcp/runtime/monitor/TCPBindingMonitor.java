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
package org.fabric3.binding.tcp.runtime.monitor;

import org.fabric3.api.annotation.logging.Info;
import org.fabric3.api.annotation.logging.Severe;

/**
 * Monitor interface to log significant events for TCP binding.
 */
public interface TCPBindingMonitor {
    /**
     * Log event of successful TCP extension started.
     * 
     * @param msg start event message
     */
    @Info
    void onTcpExtensionStarted(String msg);

    /**
     * Log event of exception occurred in TCP extension.
     * 
     * @param msg Error message
     * @param throwable exception
     */
    @Severe
    void onException(String msg, Throwable throwable);

}
