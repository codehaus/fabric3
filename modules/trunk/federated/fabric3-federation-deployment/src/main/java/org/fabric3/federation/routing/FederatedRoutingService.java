/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.federation.routing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.command.ZoneDeploymentCommand;
import org.fabric3.spi.classloader.MultiClassLoaderObjectOutputStream;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.domain.RoutingException;
import org.fabric3.spi.domain.RoutingMonitor;
import org.fabric3.spi.domain.RoutingService;
import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.topology.DomainManager;
import org.fabric3.spi.topology.MessageException;

/**
 * A routing service implementation that routes commands to a zone.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class FederatedRoutingService implements RoutingService {
    private final DomainManager domainManager;
    private final RoutingMonitor monitor;

    public FederatedRoutingService(@Reference DomainManager domainManager, @Monitor RoutingMonitor monitor) {
        this.domainManager = domainManager;
        this.monitor = monitor;
    }

    public void route(CommandMap commandMap) throws RoutingException {
        String id = commandMap.getId();
        for (String zone : commandMap.getZones()) {
            try {
                List<Command> commands = commandMap.getCommandsForZone(zone);
                monitor.routeCommands(zone);
                String correlationId = commandMap.getCorrelationId();
                boolean synchronization = commandMap.isSynchornization();
                Command command = new ZoneDeploymentCommand(id, commands, correlationId, synchronization);
                ByteArrayOutputStream bas = new ByteArrayOutputStream();
                MultiClassLoaderObjectOutputStream stream = new MultiClassLoaderObjectOutputStream(bas);
                stream.writeObject(command);
                domainManager.sendMessage(zone, bas.toByteArray());
            } catch (IOException e) {
                throw new RoutingException(e);
            } catch (MessageException e) {
                throw new RoutingException(e);
            }
        }

    }

}
