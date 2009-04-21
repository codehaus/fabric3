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
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import org.fabric3.binding.net.provision.TransportType;
import org.fabric3.binding.net.runtime.http.HttpClientPipelineFactory;
import org.fabric3.binding.net.runtime.http.HttpRequestResponseInterceptor;
import org.fabric3.binding.net.runtime.http.HttpResponseHandler;
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

    public void testServiceDispatch() throws Exception {
//        URLConnection connection = new URL("http://localhost:8989/service").openConnection();
//        connection.setDoOutput(true);
//        connection.setDoInput(true);
//        OutputStream os = connection.getOutputStream();
//        os.write("test".getBytes());
//        os.flush();
//        os.close();
//        InputStream is = connection.getInputStream();
//

        ChannelFactory factory =
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool());

        ClientBootstrap bootstrap = new ClientBootstrap(factory);

        HttpResponseHandler handler = new HttpResponseHandler();
        HttpClientPipelineFactory pipeline = new HttpClientPipelineFactory(handler);
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
        TransportServiceMonitor monitor = EasyMock.createNiceMock(TransportServiceMonitor.class);
        TransportServiceImpl service = new TransportServiceImpl(null, monitor);
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

        service.register(TransportType.HTTP, "/service", wire);
    }

}
