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
package org.fabric3.jetty;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;

import org.fabric3.spi.host.ServletHost;

/**
 * Implementations provide a Jetty transport service to the runtime
 *
 * @version $$Rev$$ $$Date$$
 */
public interface JettyService extends ServletHost {

    /**
     * Returns the active Jetty server
     *
     * @return the active Jetty server
     */
    Server getServer();

    /**
     * Registers a handler with the Jetty service.
     *
     * @param handler the handler to register
     */
    void registerHandler(Handler handler);


}
