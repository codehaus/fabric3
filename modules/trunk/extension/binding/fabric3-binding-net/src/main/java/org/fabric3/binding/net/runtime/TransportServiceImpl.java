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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.timeout.HashedWheelTimer;
import org.jboss.netty.handler.timeout.Timer;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.net.runtime.http.HttpRequestHandler;
import org.fabric3.binding.net.runtime.http.HttpServerPipelineFactory;
import org.fabric3.binding.net.runtime.http.WireHolder;
import org.fabric3.binding.net.runtime.tcp.TcpPipelineFactory;
import org.fabric3.binding.net.runtime.tcp.TcpRequestHandler;
import org.fabric3.host.work.WorkScheduler;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.services.serializer.SerializationException;
import org.fabric3.spi.services.serializer.Serializer;
import org.fabric3.spi.services.serializer.SerializerFactory;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 */
public class TransportServiceImpl implements TransportService {
    private final WorkScheduler scheduler;
    private CommunicationsMonitor monitor;

    private long connectTimeout = 10000;
    private String ipAddress = "127.0.0.1";

    private int httpPort = 8282;
    private int tcpPort = 8383;
    private String httpWireFormat = "jdk";
    private String tcpWireFormat = "jdk";
    private Map<String, SerializerFactory> serializerFactories = new HashMap<String, SerializerFactory>();


    private Timer timer;
    private ChannelFactory factory;
    private Channel httpChannel;
    private Channel tcpChannel;
    private HttpRequestHandler httpRequestHandler;
    private TcpRequestHandler tcpRequestHandler;

    public TransportServiceImpl(@Reference WorkScheduler scheduler, @Monitor CommunicationsMonitor monitor) {
        this.scheduler = scheduler;
        this.monitor = monitor;
    }

    @Reference
    public void setSerializerFactories(Map<String, SerializerFactory> serializerFactories) {
        this.serializerFactories = serializerFactories;
    }

    @Property(required = false)
    public void setConnectTimeout(long timeout) {
        this.connectTimeout = timeout;
    }

    @Property(required = false)
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Property(required = false)
    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    @Property(required = false)
    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    @Property(required = false)
    public void setTcpWireFormat(String tcpWireFormat) {
        this.tcpWireFormat = tcpWireFormat;
    }

    @Property(required = false)
    public void setHttpWireFormat(String httpWireFormat) {
        this.httpWireFormat = httpWireFormat;
    }

    @Init
    public void init() {
        factory = new NioServerSocketChannelFactory(scheduler, scheduler);
        timer = new HashedWheelTimer();
    }

    @Destroy
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public void destroy() {
        if (httpChannel != null) {
            ChannelFuture future = httpChannel.close();
            future.awaitUninterruptibly();
            if (!future.isSuccess()) {
                monitor.error(future.getCause());
            }
            // Don't release resources as it waits for the core threadpool to cease operations
            // factory.releaseExternalResources();
        }
        if (tcpChannel != null) {
            ChannelFuture future = tcpChannel.close();
            future.awaitUninterruptibly();
            if (!future.isSuccess()) {
                monitor.error(future.getCause());
            }
        }
    }

    public void registerHttp(String path, WireHolder wireHolder) throws WiringException {
        if (httpRequestHandler == null) {
            createHttpChannel();
        }
        httpRequestHandler.register(path, wireHolder);
    }

    public void registerTcp(String path, String callbackUri, Wire wire) throws WiringException {
        if (tcpRequestHandler == null) {
            createTcpChannel();
        }
        tcpRequestHandler.register(path, callbackUri, wire);

    }

    public void unregisterHttp(String path) {
        httpRequestHandler.unregister(path);
    }

    public void unregisterTcp(String path) {
        tcpRequestHandler.unregister(path);
    }

    private void createHttpChannel() throws WiringException {
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        Serializer serializer = getSerializer(httpWireFormat);
        httpRequestHandler = new HttpRequestHandler(serializer, monitor);
        HttpServerPipelineFactory pipeline = new HttpServerPipelineFactory(httpRequestHandler, timer, connectTimeout);
        bootstrap.setPipelineFactory(pipeline);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        // TODO  configure following integer vals:
        // receiveBufferSize, sendBufferSize, writeBufferHighWaterMark, writeBufferLowWaterMark, writeSpinCount, receiveBufferSizePredictor

        // Bind and start to accept incoming connections.
        InetSocketAddress socketAddress = new InetSocketAddress(ipAddress, httpPort);
        httpChannel = bootstrap.bind(socketAddress);
    }

    private void createTcpChannel() throws WiringException {
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        Serializer serializer = getSerializer(tcpWireFormat);
        tcpRequestHandler = new TcpRequestHandler(serializer, monitor);
        TcpPipelineFactory pipeline = new TcpPipelineFactory(tcpRequestHandler, timer, connectTimeout);
        bootstrap.setPipelineFactory(pipeline);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        // TODO  configure following integer vals:
        // receiveBufferSize, sendBufferSize, writeBufferHighWaterMark, writeBufferLowWaterMark, writeSpinCount, receiveBufferSizePredictor

        // Bind and start to accept incoming connections.
        InetSocketAddress socketAddress = new InetSocketAddress(ipAddress, tcpPort);
        tcpChannel = bootstrap.bind(socketAddress);
    }

    //FIXME get rid of this method and replace with header serializer type
    private Serializer getSerializer(String wireFormat) throws WiringException {
        SerializerFactory factory = serializerFactories.get(wireFormat);
        if (factory == null) {
            throw new WiringException("Serializer not found for: " + wireFormat);
        }
        try {
            return factory.getInstance(Collections.<Class<?>>emptySet(), Collections.<Class<?>>emptySet());
        } catch (SerializationException e) {
            throw new WiringException(e);
        }

    }
}
