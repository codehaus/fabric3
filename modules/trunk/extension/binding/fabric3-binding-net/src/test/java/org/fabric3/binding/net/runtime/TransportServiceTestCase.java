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
package org.fabric3.binding.net.runtime;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.timeout.HashedWheelTimer;
import org.jboss.netty.handler.timeout.Timer;

import org.fabric3.binding.net.provision.TransportType;
import org.fabric3.binding.net.runtime.http.HttpClientPipelineFactory;
import org.fabric3.binding.net.runtime.http.HttpRequestResponseInterceptor;
import org.fabric3.binding.net.runtime.http.HttpResponseHandler;
import org.fabric3.host.work.DefaultPausableWork;
import org.fabric3.host.work.WorkScheduler;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 */
public class TransportServiceTestCase extends TestCase {
    private CommunicationsMonitor monitor;

    public void testServiceDispatch() throws Exception {
        ChannelFactory factory =
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool());

        ClientBootstrap bootstrap = new ClientBootstrap(factory);

        HttpResponseHandler handler = new HttpResponseHandler(10, monitor);
        Timer timer = new HashedWheelTimer();
        HttpClientPipelineFactory pipeline = new HttpClientPipelineFactory(handler, timer, 10000);
        bootstrap.setPipelineFactory(pipeline);

        HttpRequestResponseInterceptor interceptor =
                new HttpRequestResponseInterceptor("/service", "foo", bootstrap, new InetSocketAddress("localhost", 8989));
        List<CallFrame> stack = new ArrayList<CallFrame>();
        MessageImpl msg = new MessageImpl();
        WorkContext context = new WorkContext();
        context.addCallFrames(stack);
        msg.setWorkContext(context);
        msg.setBody("test");
        Message ret = interceptor.invoke(msg);
        assertEquals("response", ret.getBody());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        monitor = EasyMock.createNiceMock(CommunicationsMonitor.class);
        TransportServiceImpl service = new TransportServiceImpl(new MockScheduler(), monitor);
        service.setHttpPort(8989);
        service.init();

        Interceptor mockInterceptor = EasyMock.createMock(Interceptor.class);
        MessageImpl msg = new MessageImpl();
        msg.setBody("response");
        EasyMock.expect(mockInterceptor.invoke(EasyMock.isA(Message.class))).andReturn(msg);
        EasyMock.replay(mockInterceptor);
        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.expect(chain.getHeadInterceptor()).andReturn(mockInterceptor);
        EasyMock.replay(chain);

        Map<PhysicalOperationDefinition, InvocationChain> chains = new HashMap<PhysicalOperationDefinition, InvocationChain>();
        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName("test");
        chains.put(operation, chain);

        Wire wire = EasyMock.createMock(Wire.class);
        EasyMock.expect(wire.getInvocationChains()).andReturn(chains).atLeastOnce();
        EasyMock.replay(wire);

        service.register(TransportType.HTTP, "/service", null, wire);
    }

    private class MockScheduler extends AbstractExecutorService implements WorkScheduler {
        private ExecutorService executor = Executors.newCachedThreadPool();

        public void shutdown() {
            executor.shutdown();
        }

        public List<Runnable> shutdownNow() {
            return executor.shutdownNow();
        }

        public boolean isShutdown() {
            return executor.isShutdown();
        }

        public boolean isTerminated() {
            return executor.isTerminated();
        }

        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return executor.awaitTermination(timeout, unit);
        }

        public void execute(Runnable command) {
            executor.execute(command);
        }

        public <T extends DefaultPausableWork> void scheduleWork(T work) {

        }
    }

}
