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
package org.fabric3.jxta.impl;

import java.net.URI;

import junit.framework.TestCase;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;

import org.easymock.classextension.EasyMock;
import org.fabric3.host.runtime.HostInfo;

/**
 * @version $Revsion$ $Date$
 */
public class JxtaServiceImplTestCase extends TestCase {

    /**
     * Tests the creation of domain group.
     */
    public void testGetDomainGroup() throws Exception{

        HostInfo hostInfo = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(hostInfo.getRuntimeId()).andReturn("runtime1");
        EasyMock.expect(hostInfo.getDomain()).andReturn(new URI("domain1"));
        EasyMock.replay(hostInfo);

        NetworkConfigurator networkConfigurator = new NetworkConfigurator();
        networkConfigurator.setPrincipal("test-user");
        networkConfigurator.setPassword("test-password");

        JxtaServiceImpl jxtaService = new JxtaServiceImpl();
        jxtaService.setHostInfo(hostInfo);
        jxtaService.setNetworkConfigurator(networkConfigurator);
        jxtaService.start();

        PeerGroup domainGroup = jxtaService.getDomainGroup();
        assertNotNull(domainGroup);
        assertEquals("domain1", domainGroup.getPeerGroupName());

    }

}
