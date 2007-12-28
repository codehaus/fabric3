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

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.deployer.Deployer;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.model.physical.PhysicalChangeSet;

/**
 * @version $Rev$ $Date$
 */
public class RuntimeRoutingServiceTestCase extends TestCase {
    public static final URI RUNTIME_ID = URI.create("localhost://runtime");
    private RuntimeRoutingService service;
    private Deployer deployer;

    public void testRouting() throws Exception {
        PhysicalChangeSet set = new PhysicalChangeSet();
        service.route(RUNTIME_ID, set);
        EasyMock.verify(deployer);
    }

    protected void setUp() throws Exception {
        super.setUp();
        deployer = EasyMock.createMock(Deployer.class);
        deployer.applyChangeSet(EasyMock.isA(PhysicalChangeSet.class));
        EasyMock.replay(deployer);
        CommandExecutorRegistry commandRegistry = EasyMock.createMock(CommandExecutorRegistry.class);
        EasyMock.replay(commandRegistry);
        service = new RuntimeRoutingService(deployer, commandRegistry);
    }
}
