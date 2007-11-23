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
package org.fabric3.host.monitor;

import java.util.Map;

/**
 * A MonitorFactory creates implementations of components' monitor interfaces that interface with a its monitoring
 * scheme. For example, a implementation may create versions that emit appropriate logging events or which send
 * notifications to a management API.
 *
 * @version $Rev$ $Date$
 */
public interface MonitorFactory {
    /**
     * Initializes MonitorFactory instances with implementation-specific configuration properties.
     *
     * @param configProperties a map of named configuration properties.  May be null.
     * @throws IllegalArgumentException if the instance can't be configured using the supplied properties
     */
    @Deprecated
    void initialize(Map<String, Object> configProperties);

    /**
     * Return a monitor for a component's monitor interface.
     *
     * @param monitorInterface the component's monitoring interface
     * @return an implementation of the monitoring interface; will not be null
     */
    <T> T getMonitor(Class<T> monitorInterface);

    @Deprecated
    void register(ExceptionFormatter formatter);
}
