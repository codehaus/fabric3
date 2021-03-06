/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime.broker;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;
import org.zeromq.ZMQ;

import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.binding.zeromq.runtime.federation.AddressCache;
import org.fabric3.binding.zeromq.runtime.federation.AddressEvent;
import org.fabric3.binding.zeromq.runtime.management.ZeroMQManagementService;
import org.fabric3.binding.zeromq.runtime.message.OneWaySender;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocator;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

/**
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class ZeroMQWireBrokerImplTestCase extends TestCase {
    private static final SocketAddress ADDRESS = new SocketAddress("runtime", "tcp", "10.10.10.1", new Port() {
        public String getName() {
            return null;
        }

        public int getNumber() {
            return 1061;
        }

        public void bind(TYPE type) {

        }

        public void release() {

        }
    });

    private ContextManager manager;
    private AddressCache addressCache;
    private ExecutorService executorService;
    private MessagingMonitor monitor;
    private ZMQ.Context context;
    private ZeroMQWireBrokerImpl broker;
    private PortAllocator allocator;
    private HostInfo info;
    private ZeroMQManagementService managementService;
    private ZeroMQMetadata metadata;


    public void testConnectToReceiverRelease() throws Exception {
        EasyMock.expect(info.getRuntimeName()).andReturn("runtime");

        addressCache.publish(EasyMock.isA(AddressEvent.class));
        EasyMock.expectLastCall().times(2);

        Port port = EasyMock.createMock(Port.class);
        EasyMock.expect(port.getNumber()).andReturn(1099).anyTimes();

        EasyMock.expect(allocator.allocate("wire", "zmq")).andReturn(port);
        allocator.release("wire");

        EasyMock.replay(manager, addressCache, executorService, monitor, allocator, info, managementService);

        PhysicalOperationDefinition definition = new PhysicalOperationDefinition();
        definition.setOneWay(true);

        Interceptor interceptor = EasyMock.createMock(Interceptor.class);
        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.expect(chain.getPhysicalOperation()).andReturn(definition);
        chain.addInterceptor(EasyMock.isA(Interceptor.class));

        EasyMock.expect(chain.getHeadInterceptor()).andReturn(interceptor).atLeastOnce();

        List<InvocationChain> chains = Collections.singletonList(chain);
        EasyMock.replay(chain, interceptor, port);

        broker.connectToReceiver(URI.create("wire"), chains, metadata, getClass().getClassLoader());
        broker.releaseReceiver(URI.create("wire"));

        EasyMock.verify(manager, addressCache, executorService, monitor, allocator, info, chain, interceptor, port, managementService);
    }

    public void testConnectToSenderRelease() throws Exception {

        EasyMock.expect(addressCache.getActiveAddresses("wire")).andReturn(Collections.singletonList(ADDRESS));
        addressCache.subscribe(EasyMock.eq("wire"), EasyMock.isA(OneWaySender.class));
        EasyMock.expectLastCall();

        EasyMock.replay(context);
        EasyMock.replay(manager, addressCache, executorService, monitor, allocator, info, managementService);

        PhysicalOperationDefinition definition = new PhysicalOperationDefinition();
        definition.setOneWay(true);

        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.expect(chain.getPhysicalOperation()).andReturn(definition);
        chain.addInterceptor(EasyMock.isA(Interceptor.class));
        EasyMock.expectLastCall().atLeastOnce();

        List<InvocationChain> chains = Collections.singletonList(chain);
        EasyMock.replay(chain);

        broker.connectToSender("id", URI.create("wire"), chains, metadata, getClass().getClassLoader());
        broker.releaseSender("id", URI.create("wire"));

        EasyMock.verify(context);
        EasyMock.verify(manager, addressCache, executorService, monitor, allocator, info, chain, managementService);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = EasyMock.createMock(ZMQ.Context.class);
        manager = EasyMock.createMock(ContextManager.class);

        addressCache = EasyMock.createMock(AddressCache.class);

        executorService = EasyMock.createMock(ExecutorService.class);

        allocator = EasyMock.createMock(PortAllocator.class);

        info = EasyMock.createMock(HostInfo.class);

        monitor = EasyMock.createNiceMock(MessagingMonitor.class);

        managementService = EasyMock.createNiceMock(ZeroMQManagementService.class);

        EventService eventService = EasyMock.createNiceMock(EventService.class);
        broker = new ZeroMQWireBrokerImpl(manager, addressCache, allocator, executorService, managementService, eventService, info, monitor);

        metadata = new ZeroMQMetadata();

    }
}
