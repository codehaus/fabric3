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
package org.fabric3.fabric.services.routing;

import java.net.URI;
import java.util.Set;

import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.command.CommandSet;

/**
 * Implementations route physical change sets to a runtime node.
 *
 * @version $Rev$ $Date$
 */
public interface RoutingService {

    /**
     * Routes a physical change set to a runtime node
     *
     * @param runtimeId the runtime node id
     * @param set       the physical change set
     * @throws RoutingException if an exception occurs during routing
     */
    void route(URI runtimeId, PhysicalChangeSet set) throws RoutingException;

    /**
     * Routes a command set to a runtime node
     *
     * @param runtimeId the runtime node id
     * @param set       the command set
     * @throws RoutingException if an exception occurs during routing
     */
    void route(URI runtimeId, CommandSet set) throws RoutingException;

}
