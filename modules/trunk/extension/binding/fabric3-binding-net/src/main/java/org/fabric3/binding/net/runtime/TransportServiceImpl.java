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
import org.fabric3.binding.net.provision.TransportType;
import org.fabric3.binding.net.runtime.http.HttpRequestHandler;
import org.fabric3.binding.net.runtime.http.HttpServerPipelineFactory;
import org.fabric3.host.work.WorkScheduler;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 */
public class TransportServiceImpl implements TransportService {
    private final WorkScheduler scheduler;
    private CommunicationsMonitor monitor;
    private ChannelFactory factory;
    private Channel httpChannel;

    private long closeTimeout = 10000;
    private String httpAddress = "127.0.0.1";
    private int httpPort = 8282;
    private HttpRequestHandler httpRequestHandler;
    private Timer timer;

    public TransportServiceImpl(@Reference WorkScheduler scheduler, @Monitor CommunicationsMonitor monitor) {
        this.scheduler = scheduler;
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setCloseTimeout(long timeout) {
        this.closeTimeout = timeout;
    }

    @Property(required = false)
    public void setHttpAddress(String httpAddress) {
        this.httpAddress = httpAddress;
    }

    @Property(required = false)
    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    @Init
    public void init() {
        factory = new NioServerSocketChannelFactory(scheduler, scheduler);
        timer = new HashedWheelTimer();
        createHttpChannel();
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
    }

    public void register(TransportType type, String path, String callbackUri, Wire wire) {
        switch (type) {
        case HTTP:
            httpRequestHandler.register(path, callbackUri, wire);
            break;
        case HTTPS:
            throw new UnsupportedOperationException();
        case TCP:
            throw new UnsupportedOperationException();
        }
    }

    public void unregister(TransportType type, String path) {
        switch (type) {
        case HTTP:
            httpRequestHandler.unregister(path);
            break;
        case HTTPS:
            throw new UnsupportedOperationException();
        case TCP:
            throw new UnsupportedOperationException();
        }
    }

    private void createHttpChannel() {
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        httpRequestHandler = new HttpRequestHandler(monitor);
        HttpServerPipelineFactory pipeline = new HttpServerPipelineFactory(httpRequestHandler, timer, closeTimeout);
        bootstrap.setPipelineFactory(pipeline);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        // TODO  configure following integer vals:
        // receiveBufferSize, sendBufferSize, writeBufferHighWaterMark, writeBufferLowWaterMark, writeSpinCount, receiveBufferSizePredictor

        // Bind and start to accept incoming connections.
        InetSocketAddress socketAddress = new InetSocketAddress(httpAddress, httpPort);
        httpChannel = bootstrap.bind(socketAddress);
    }

}
