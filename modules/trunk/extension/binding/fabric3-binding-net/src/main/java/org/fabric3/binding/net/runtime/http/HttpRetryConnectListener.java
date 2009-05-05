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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

import org.fabric3.binding.net.provision.NetConstants;
import org.fabric3.binding.net.NetBindingMonitor;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.binding.serializer.Serializer;

/**
 * Listens for an HTTP channel connection event, retrying a specified number of times if the operation failed.
 *
 * @version $Revision$ $Date$
 */
public class HttpRetryConnectListener implements ChannelFutureListener {
    private Message msg;
    private String url;
    private SocketAddress address;
    private String operationName;
    private Serializer headerSerializer;
    private Serializer inputSerializer;
    private ClientBootstrap bootstrap;
    private int maxRetry;
    private NetBindingMonitor monitor;
    private AtomicInteger retryCount;

    public HttpRetryConnectListener(Message msg,
                                    String url,
                                    SocketAddress address,
                                    String operationName,
                                    Serializer headerSerializer,
                                    Serializer inputSerializer,
                                    ClientBootstrap bootstrap,
                                    int maxRetry,
                                    NetBindingMonitor monitor) {
        this.msg = msg;
        this.url = url;
        this.address = address;
        this.operationName = operationName;
        this.headerSerializer = headerSerializer;
        this.inputSerializer = inputSerializer;
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
        // connection succeeded, write data

        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, url);

        request.addHeader(NetConstants.OPERATION_NAME, operationName);

        List<CallFrame> stack = msg.getWorkContext().getCallFrameStack();
        if (!stack.isEmpty()) {
            String serialized = headerSerializer.serialize(String.class, stack);
            request.addHeader(NetConstants.ROUTING, serialized);
        }
        Object body = msg.getBody();

        if (body != null) {
            String str;
            if (body.getClass().isArray()) {
                Object[] payload = (Object[]) body;
                if (payload.length > 1) {
                    throw new UnsupportedOperationException("Multiple parameters not supported");
                }
                str = inputSerializer.serialize(String.class, payload[0]);
            } else {
                str = inputSerializer.serialize(String.class, body);
            }
            request.addHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(str.length()));
            ChannelBuffer buf = ChannelBuffers.copiedBuffer(str, "UTF-8");
            request.setContent(buf);
        }

        ChannelFuture writeFuture = channel.write(request);
        writeFuture.addListener(new HttpRetryWriteListener(request, maxRetry, monitor));

    }

}
