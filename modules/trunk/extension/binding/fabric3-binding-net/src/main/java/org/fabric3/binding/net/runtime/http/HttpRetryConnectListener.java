/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ÒLicenseÓ), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an Òas isÓ basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.net.runtime.http;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.HttpRequest;

import org.fabric3.binding.net.NetBindingMonitor;

/**
 * Listens for an HTTP channel connection event, retrying a specified number of times if the operation failed.
 *
 * @version $Revision$ $Date$
 */
public class HttpRetryConnectListener implements ChannelFutureListener {
    private HttpRequest request;
    private String encodedMessage;
    private SocketAddress address;
    private ClientBootstrap bootstrap;
    private int maxRetry;
    private NetBindingMonitor monitor;
    private AtomicInteger retryCount;

    public HttpRetryConnectListener(HttpRequest request,
                                    String encodedMessage,
                                    SocketAddress address,
                                    ClientBootstrap bootstrap,
                                    int maxRetry,
                                    NetBindingMonitor monitor) {
        this.request = request;
        this.encodedMessage = encodedMessage;
        this.address = address;
        this.bootstrap = bootstrap;
        this.maxRetry = maxRetry;
        this.monitor = monitor;
        retryCount = new AtomicInteger(0);
    }

    public void operationComplete(ChannelFuture future) throws Exception {
        Channel channel = future.getChannel();
        if (!future.isSuccess() && retryCount.getAndIncrement() >= maxRetry) {
            // connection failed and max number of retries exceeded
            monitor.error(future.getCause());
            return;
        }

        if (!future.isSuccess()) {
            // retry the connection
            ChannelFuture openFuture = bootstrap.connect(address);
            openFuture.addListener(this);
            return;
        }
        // connection succeeded, write the message
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(encodedMessage, "UTF-8");
        request.setContent(buf);
        ChannelFuture writeFuture = channel.write(request);
        HttpRetryWriteListener listener = new HttpRetryWriteListener(request, maxRetry, monitor);
        writeFuture.addListener(listener);

    }

}
