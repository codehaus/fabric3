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
package org.fabric3.binding.aq.monitor;


import org.fabric3.api.annotation.LogLevel;

/**
 * Monitor interface for AQ
 * @version $Rev: 3137 $ $Date: 2008-03-18 02:31:06 +0800 (Tue, 18 Mar 2008) $
 */
public interface AQMonitor {
           
    
    /**
     * Message when in Source Wire
     */
    @LogLevel("INFO")
    void onSourceWire(String message);
    
    /**
     * Message when in Target
     */
    @LogLevel("INFO")
    void onTargetWire(String message);
    
    /**
     * Log message to stop on AQ
     */
    @LogLevel("INFO")
    void stopOnAQHost(String message);
    
    /**
     * Log message to stop when consumers are stopped
     */
    @LogLevel("INFO")
    void stopConsumer(String message);
    
    /**
     * Logs the Exception
     * @param exception
     */
    @LogLevel("INFO")
    void onException(Throwable exception);
}
