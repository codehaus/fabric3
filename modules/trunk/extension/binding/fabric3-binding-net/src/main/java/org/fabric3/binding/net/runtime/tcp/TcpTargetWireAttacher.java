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
package org.fabric3.binding.net.runtime.tcp;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.timeout.HashedWheelTimer;
import org.jboss.netty.handler.timeout.Timer;
import org.oasisopen.sca.annotation.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.net.provision.TcpWireTargetDefinition;
import org.fabric3.binding.net.runtime.CommunicationsMonitor;
import org.fabric3.binding.net.runtime.OneWayClientHandler;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.services.serializer.Serializer;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches references configured to use the TCP binding to the channel and handler pipeline.
 *
 * @version $Revision$ $Date$
 */
public class TcpTargetWireAttacher implements TargetWireAttacher<TcpWireTargetDefinition> {
    private long closeTimeout = 10000;
    private CommunicationsMonitor monitor;
    private ChannelFactory factory;
    private Timer timer;
    private Map<String, Serializer> serializers;
    private String tcpWireFormat = "jdk";

    public TcpTargetWireAttacher(@Monitor CommunicationsMonitor monitor) {
        this.monitor = monitor;
    }

    @Reference
    public void setSerializers(Map<String, Serializer> serializers) {
        this.serializers = serializers;
    }

    // FIXME this should be configured to same value as TransportServiceImpl
    @Property(required = false)
    public void setCloseTimeout(long timeout) {
        this.closeTimeout = timeout;
    }

    @Property(required = false)
    public void setTcpWireFormat(String tcpWireFormat) {
        this.tcpWireFormat = tcpWireFormat;
    }

    @Init
    public void init() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        factory = new NioClientSocketChannelFactory(executorService, executorService);
        timer = new HashedWheelTimer();
    }

    @Destroy
    public void destroy() {
        if (factory != null) {
            factory.releaseExternalResources();
        }
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, TcpWireTargetDefinition target, Wire wire) throws WiringException {

        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            if (entry.getKey().isOneWay()) {
                attachOneWay(target, entry.getKey(), entry.getValue());
            } else {
                attachRequestResponse(target, entry.getKey(), entry.getValue());
            }
        }
    }

    public void detachFromTarget(PhysicalWireSourceDefinition source, TcpWireTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(TcpWireTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    private void attachOneWay(TcpWireTargetDefinition target, PhysicalOperationDefinition operation, InvocationChain chain)
            throws WiringException {
        ClientBootstrap bootstrap = new ClientBootstrap(factory);

        OneWayClientHandler handler = new OneWayClientHandler(monitor);
        TcpPipelineFactory pipeline = new TcpPipelineFactory(handler, timer, closeTimeout);
        bootstrap.setPipelineFactory(pipeline);

        URI uri = target.getUri();
        String path = uri.getPath();

        InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
        // TODO support method overloading
        String name = operation.getName();
        Serializer serializer = serializers.get(tcpWireFormat);
        if (serializer == null) {
            throw new WiringException("Serializer not found for: " + tcpWireFormat);
        }
        TcpOneWayInterceptor interceptor = new TcpOneWayInterceptor(path, name, bootstrap, address, serializer, monitor);
        chain.addInterceptor(interceptor);
    }

    private void attachRequestResponse(TcpWireTargetDefinition target,
                                       PhysicalOperationDefinition operation,
                                       InvocationChain chain) throws WiringException {
        ClientBootstrap bootstrap = new ClientBootstrap(factory);

        Serializer serializer = serializers.get(tcpWireFormat);
        if (serializer == null) {
            throw new WiringException("Serializer not found for: " + tcpWireFormat);
        }
        TcpResponseHandler handler = new TcpResponseHandler(serializer, closeTimeout, monitor);
        TcpPipelineFactory pipeline = new TcpPipelineFactory(handler, timer, closeTimeout);
        bootstrap.setPipelineFactory(pipeline);

        URI uri = target.getUri();
        String path = uri.getPath();

        InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
        // TODO support method overloading
        String name = operation.getName();
        TcpRequestResponseInterceptor interceptor = new TcpRequestResponseInterceptor(path, name, serializer, bootstrap, address);
        chain.addInterceptor(interceptor);
    }


}