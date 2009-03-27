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
package org.fabric3.fabric.generator.extension;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.command.ProvisionExtensionsCommand;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;

/**
 * @version $Revision$ $Date$
 */
public class ExtensionGeneratorImplTestCase extends TestCase {

    private ExtensionGenerator generator;
    private MetaDataStore store;

    public void testResolve() throws Exception {

        Set<Contribution> extensions = new HashSet<Contribution>();
        URI extensionUri = URI.create("foo");
        extensions.add(new Contribution(extensionUri));

        List<Contribution> contributions = new ArrayList<Contribution>();
        URI contributionUri = URI.create("app");
        contributions.add(new Contribution(contributionUri));
        Map<String, List<Contribution>> map = new HashMap<String, List<Contribution>>();
        map.put("zone1", contributions);

        EasyMock.expect(store.resolveCapabilities(EasyMock.isA(Contribution.class))).andReturn(extensions);
        EasyMock.replay(store);
        Map<String, Command> ret = generator.generate(map, true);
        Command commands = ret.get("zone1");
        ProvisionExtensionsCommand command = (ProvisionExtensionsCommand) commands;
        assertEquals(extensionUri, command.getExtensionUris().get(0));

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        store = EasyMock.createMock(MetaDataStore.class);
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.CONTROLLER);
        EasyMock.replay(info);
        generator = new ExtensionGeneratorImpl(store, info);
    }
}
