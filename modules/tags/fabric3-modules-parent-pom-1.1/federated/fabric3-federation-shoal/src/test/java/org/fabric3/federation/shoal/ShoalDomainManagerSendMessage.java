/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.federation.shoal;

/**
 * @version $Revision$ $Date$
 */
public class ShoalDomainManagerSendMessage {
//    private ShoalDomainManager domainManager;
//
//    public static void main(String[] args) throws Exception {
//        ShoalDomainManagerSendMessage client = new ShoalDomainManagerSendMessage();
//        client.init();
//        while (true) {
//            System.out.println("Press <Enter> to send messages, 'x' to exit...");
//            int key = System.in.read();
//            if (key == 88 || key == 120) {
//                System.exit(0);
//            }
//            client.sendMessages();
//        }
//    }
//
//    public void sendMessages() throws Exception {
//        for (Zone zone : domainManager.getZones()) {
//            String zoneName = zone.getName();
//            System.out.println("Sending message to zone: " + zoneName);
//            Command command = new MockCommand();
//            ByteArrayOutputStream bas = new ByteArrayOutputStream();
//            MultiClassLoaderObjectOutputStream stream = new MultiClassLoaderObjectOutputStream(bas);
//            stream.writeObject(command);
//            domainManager.sendMessage(zoneName, bas.toByteArray());
//        }
//    }
//
//    @SuppressWarnings({"unchecked"})
//    protected void init() throws Exception {
//        EventService eventService = EasyMock.createNiceMock(EventService.class);
//        HostInfo info = EasyMock.createMock(HostInfo.class);
//        EasyMock.expect(info.getDomain()).andReturn(URI.create("fabric3://domain"));
//        EasyMock.replay(info);
//        FederationServiceMonitor monitor = new MockMonitor();
//        FederationServiceImpl federationService = new FederationServiceImpl(eventService, info, monitor);
//        federationService.setController(true);
//        federationService.setRuntimeName("Controller");
//        federationService.init();
//
//        DomainManagerMonitor domainMonitor = EasyMock.createNiceMock(DomainManagerMonitor.class);
//        EasyMock.replay(domainMonitor);
//
//        domainManager = new ShoalDomainManager(federationService, null, null, domainMonitor);
//        domainManager.init();
//
//        federationService.onJoinDomain();
//        Thread.sleep(4000);
//    }
//
//    private static class MockCommand implements Command {
//        private static final long serialVersionUID = -6379269046748362803L;
//
//        public int getOrder() {
//            return 0;
//        }
//
//        public int compareTo(Command o) {
//            return 0;
//        }
//    }
}
