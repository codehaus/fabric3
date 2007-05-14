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
import java.util.Collections;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.deployer.Deployer;
import org.fabric3.spi.component.RegistrationException;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.host.runtime.HostInfo;

/**
 * A routing service implementation that routes to the local runtime instance. For example, this service is used to
 * route changesets for runtime extensions.
 *
 * @version $Rev$ $Date$
 */
public class RuntimeRoutingService implements RoutingService {
    private final Deployer deployer;
    private final Set<String> runtimeIds;

    public RuntimeRoutingService(@Reference Deployer deployer,
                                 @Reference HostInfo hostInfo) {
        this.deployer = deployer;
        runtimeIds = Collections.singleton(hostInfo.getRuntimeId());
    }

    public void route(URI runtimeId, PhysicalChangeSet set) throws RoutingException {
        try {
            deployer.applyChangeSet(set);
        } catch (BuilderException e) {
            throw new RoutingException(e);
        } catch (RegistrationException e) {
            throw new RoutingException(e);
        }
    }

    public Set<String> getRuntimeIds() {
        return runtimeIds;
    }
}
