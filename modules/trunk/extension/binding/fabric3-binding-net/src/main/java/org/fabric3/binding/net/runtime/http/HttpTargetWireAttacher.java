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
package org.fabric3.binding.net.runtime.http;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.oasisopen.sca.annotation.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;

import org.fabric3.binding.net.provision.HttpWireTargetDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches references configured to use the HTTP binding to the channel and handler pipeline.
 *
 * @version $Revision$ $Date$
 */
public class HttpTargetWireAttacher implements TargetWireAttacher<HttpWireTargetDefinition> {
    private ChannelFactory factory;
    private String httpAddress = "127.0.0.1";
    private int httpPort = 8282;

    // FIXME this should be configured to same value as TransportServiceImpl
    @Property(required = false)
    public void setHttpAddress(String httpAddress) {
        this.httpAddress = httpAddress;
    }

    // FIXME this should be configured to same value as TransportServiceImpl
    @Property(required = false)
    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    @Init
    public void init() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        // TODO integrate with server thread pooling
        factory = new NioClientSocketChannelFactory(executorService, executorService);
    }

    @Destroy
    public void destroy() {
        if (factory != null) {
            factory.releaseExternalResources();
        }
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, HttpWireTargetDefinition target, Wire wire) throws WiringException {

        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            if (entry.getKey().isOneWay()) {
                attachOneWay(target, entry.getKey(), entry.getValue());
            } else {
                attachRequestResponse(target, entry.getKey(), entry.getValue());
            }
        }
    }

    public void detachFromTarget(PhysicalWireSourceDefinition source, HttpWireTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(HttpWireTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    private void attachOneWay(HttpWireTargetDefinition target, PhysicalOperationDefinition operation, InvocationChain chain)
            throws WiringException {
        ClientBootstrap bootstrap = new ClientBootstrap(factory);

        SimpleChannelHandler handler = new SimpleChannelHandler();
        HttpClientPipelineFactory pipeline = new HttpClientPipelineFactory(handler);
        bootstrap.setPipelineFactory(pipeline);

        URI uri = target.getUri();
        String path = uri.getPath();

        InetSocketAddress address = new InetSocketAddress(httpAddress, httpPort);
        // TODO support method overloading
        String name = operation.getName();
        HttpOneWayInterceptor interceptor = new HttpOneWayInterceptor(path, name, bootstrap, address);
        chain.addInterceptor(interceptor);
    }

    private void attachRequestResponse(HttpWireTargetDefinition target,
                                       PhysicalOperationDefinition operation,
                                       InvocationChain chain) throws WiringException {
        ClientBootstrap bootstrap = new ClientBootstrap(factory);

        HttpResponseHandler handler = new HttpResponseHandler();
        HttpClientPipelineFactory pipeline = new HttpClientPipelineFactory(handler);
        bootstrap.setPipelineFactory(pipeline);

        URI uri = target.getUri();
        String path = uri.getPath();

        InetSocketAddress address = new InetSocketAddress(httpAddress, httpPort);
        // TODO support method overloading
        String name = operation.getName();
        HttpRequestResponseInterceptor interceptor = new HttpRequestResponseInterceptor(path, name, bootstrap, address);
        chain.addInterceptor(interceptor);
    }


}
