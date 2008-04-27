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
package org.fabric3.spi.services.discovery;

import java.util.Set;
import java.net.URI;

import org.fabric3.spi.model.topology.RuntimeInfo;

/**
 * Defines the abstraction for getting domain wide information of nodes
 * participating in the federated domain.
 *
 * @version $Revsion$ $Date$
 */
public interface DiscoveryService {

    /**
     * Returns information on the nodes participating in the same domain
     * as the current node. Each element in the returned list will
     * correspond to a federated runtime participating in the domain.
     *
     * @return List of runtimes participating in the domain.
     */
    Set<RuntimeInfo> getParticipatingRuntimes();

    /**
     * Gets the runtime info for the given runtime id.
     *
     * @param runtimeId Runtime id.
     * @return Runtime info.
     */
    RuntimeInfo getRuntimeInfo(URI runtimeId);

    void joinDomain(long timeout) throws DiscoveryException;

}
