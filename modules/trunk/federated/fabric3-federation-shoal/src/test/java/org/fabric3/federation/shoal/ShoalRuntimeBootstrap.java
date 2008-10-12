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
package org.fabric3.federation.shoal;

import java.net.URI;
import javax.xml.namespace.QName;

import org.easymock.EasyMock;
import org.easymock.IAnswer;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.Constants;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.event.EventService;

/**
 * @version $Revision$ $Date$
 */
public class ShoalRuntimeBootstrap {
    private ShoalRuntimeManager runtimeManager;

    public static void main(String[] args) throws Exception {
        ShoalRuntimeBootstrap client = new ShoalRuntimeBootstrap();
        client.init();
        while (true) {
            System.out.println("Press 'x' to exit...");
            int key = System.in.read();
            if (key == 88 || key == 120) {
                System.exit(0);
            }
        }
    }

    protected void init() throws Exception {
        EventService eventService = EasyMock.createNiceMock(EventService.class);

        FederationServiceMonitor monitor = new MockMonitor();

        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getDomain()).andReturn(URI.create("fabric3://domain"));
        EasyMock.replay(info);

        FederationServiceImpl federationService = new FederationServiceImpl(eventService, info, monitor);
        federationService.setRuntimeName("Runtime");
        federationService.init();

        CommandExecutorRegistry executorRegistry = EasyMock.createNiceMock(CommandExecutorRegistry.class);
        executorRegistry.execute(EasyMock.isA(Command.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                System.out.println("Received command: " + EasyMock.getCurrentArguments()[0]);
                return null;
            }
        });
        EasyMock.replay(executorRegistry);

        ClassLoaderRegistry classLoaderRegistry = EasyMock.createNiceMock(ClassLoaderRegistry.class);
        EasyMock.replay(classLoaderRegistry);

        runtimeManager = new ShoalRuntimeManager(federationService, executorRegistry, classLoaderRegistry);
//        runtimeManager.addTransportMetadata(new QName(Constants.FABRIC3_NS, "http"), "Http information");
        runtimeManager.init();

        federationService.onJoinDomain();
    }
}

