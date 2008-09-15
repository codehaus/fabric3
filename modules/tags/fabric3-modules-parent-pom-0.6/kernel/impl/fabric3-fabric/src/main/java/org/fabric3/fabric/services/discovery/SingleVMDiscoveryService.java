/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.fabric.services.discovery;

import java.util.Collections;
import java.util.Set;
import java.net.URI;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.spi.services.runtime.RuntimeInfoService;

/**
 * A single node discovery service
 *
 * @version $Rev$ $Date$
 */
public class SingleVMDiscoveryService implements DiscoveryService {
    private RuntimeInfoService service;

    public SingleVMDiscoveryService(@Reference RuntimeInfoService service) {
        this.service = service;
    }

    public Set<RuntimeInfo> getParticipatingRuntimes() {
        return Collections.emptySet();
    }

    public RuntimeInfo getRuntimeInfo(URI runtimeId) {
        return service.getRuntimeInfo();
    }

    public void joinDomain(long timeout) {

    }
}
