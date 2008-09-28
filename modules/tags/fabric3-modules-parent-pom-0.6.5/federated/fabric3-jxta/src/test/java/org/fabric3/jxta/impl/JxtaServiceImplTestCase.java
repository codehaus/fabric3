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
 */
package org.fabric3.jxta.impl;

import java.net.URI;

import junit.framework.TestCase;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import org.easymock.classextension.EasyMock;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.runtime.RuntimeInfoService;

/**
 * @version $Revsion$ $Date$
 */
public class JxtaServiceImplTestCase extends TestCase {

    /**
     * Tests the creation of domain group.
     */
    public void testGetDomainGroup() throws Exception {
        RuntimeInfoService infoService = EasyMock.createMock(RuntimeInfoService.class);
        EasyMock.expect(infoService.getCurrentRuntimeId()).andReturn(URI.create("jxta://rumtime1"));
        EasyMock.replay(infoService);

        HostInfo hostInfo = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(hostInfo.getDomain()).andReturn(new URI("domain1"));
        EasyMock.replay(hostInfo);

        NetworkConfigurator networkConfigurator = new NetworkConfigurator();
        networkConfigurator.setPrincipal("test-user");
        networkConfigurator.setPassword("test-password");

        JxtaServiceImpl jxtaService = new JxtaServiceImpl();
        jxtaService.setHostInfo(hostInfo);
        jxtaService.setNetworkConfigurator(networkConfigurator);
        jxtaService.setRuntimeInfoService(infoService);
        jxtaService.start();

        PeerGroup domainGroup = jxtaService.getDomainGroup();
        assertNotNull(domainGroup);
        assertEquals("domain1", domainGroup.getPeerGroupName());

    }

}
