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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Set;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.scdl.Scope;
import org.fabric3.services.xmlfactory.XMLFactory;
import org.fabric3.services.xmlfactory.XMLFactoryInstantiationException;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.services.marshaller.MarshalException;
import org.fabric3.spi.services.marshaller.MarshalService;
import org.fabric3.spi.services.messaging.MessagingException;
import org.fabric3.spi.services.messaging.MessagingService;

/**
 * A routing service implementation that routes physical changesets across a domain
 *
 * @version $Rev$ $Date$
 */
public class FederatedRoutingService implements RoutingService {

    private MarshalService marshalService;
    private final MessagingService messagingService;
    private final CommandExecutorRegistry executorRegistry;
    private final XMLFactory xmlFactory;
    private final RoutingMonitor monitor;
    private final ScopeRegistry scopeRegistry;

    public FederatedRoutingService(@Reference MessagingService messagingService,
                                   @Reference CommandExecutorRegistry executorRegistry,
                                   @Reference XMLFactory xmlFactory,
                                   @Monitor RoutingMonitor monitor,
                                   @Reference ScopeRegistry scopeRegistry) {

        this.messagingService = messagingService;
        this.executorRegistry = executorRegistry;
        this.xmlFactory = xmlFactory;
        this.monitor = monitor;
        this.scopeRegistry = scopeRegistry;
    }

    /**
     * Used to lazily inject the MarhsalService since it may be provided by a runtime extension loaded after this component.
     *
     * @param marshalService the MarshalService to inject
     */
    @Reference(required = false)
    public void setMarshalService(MarshalService marshalService) {
        this.marshalService = marshalService;
    }

    public void route(CommandMap commandMap) throws RoutingException {

        for (URI runtimeId : commandMap.getRuntimeIds()) {

            Set<Command> commands = commandMap.getCommandsForRuntime(runtimeId);
            if (runtimeId != null) {
                monitor.routeCommands(runtimeId.toString());
                routeToDestination(runtimeId, commands);
            } else {
                routeLocally(commands);
            }

        }

    }

    private void routeLocally(Set<Command> commands) throws RoutingException {

        for (Command command : commands) {
            try {
                executorRegistry.execute(command);
            } catch (ExecutionException e) {
                throw new RoutingException(e);
            }
        }

        try {
            scopeRegistry.getScopeContainer(Scope.COMPOSITE).reinject();
        } catch (InstanceLifecycleException e) {
            throw new RoutingException(e);
        }

    }

    private void routeToDestination(URI runtimeId, Object commandSet) throws RoutingException {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XMLOutputFactory factory = xmlFactory.newOutputFactoryInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(out);
            getMarshalService().marshall(commandSet, writer);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            XMLStreamReader pcsReader = xmlFactory.newInputFactoryInstance().createXMLStreamReader(in);
            messagingService.sendMessage(runtimeId, pcsReader);
        } catch (XMLFactoryInstantiationException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            throw new RoutingException("Routing error", e);
        } catch (MarshalException e) {
            throw new RoutingException("Routing error", e);
        } catch (MessagingException e) {
            throw new RoutingException("Routing error", e);
        }

    }

    private MarshalService getMarshalService() {
        if (marshalService == null) {
            throw new IllegalStateException("MarshalService not configured");
        }
        return marshalService;
    }

}
