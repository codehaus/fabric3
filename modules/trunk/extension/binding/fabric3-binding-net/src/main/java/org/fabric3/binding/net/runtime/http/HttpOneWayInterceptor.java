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

import java.net.SocketAddress;
import java.util.List;

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
import org.fabric3.binding.net.runtime.CommunicationsMonitor;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.services.serializer.Serializer;
import org.fabric3.spi.wire.Interceptor;

/**
 * Propagates non-blocking invocations made by a client over an HTTP channel. This interceptor is placed on the reference side of an invocation
 * chain.
 *
 * @version $Revision$ $Date$
 */
public class HttpOneWayInterceptor implements Interceptor {
    private static final Message MESSAGE = new MessageImpl();
    private String operationName;
    private ClientBootstrap boostrap;
    private SocketAddress address;
    private Serializer headerSerializer;
    private Serializer inputSerializer;
    private CommunicationsMonitor monitor;
    private String url;

    /**
     * Constructor.
     *
     * @param url              the target service URL
     * @param operationName    the name of the operation being invoked
     * @param address          the target service address
     * @param headerSerializer serializes header information
     * @param inputSerializer  serializes input parameters
     * @param boostrap         the Netty ClientBootstrap instance for sending invocations
     * @param monitor          the event monitor
     */
    public HttpOneWayInterceptor(String url,
                                 String operationName,
                                 SocketAddress address,
                                 Serializer headerSerializer,
                                 Serializer inputSerializer,
                                 ClientBootstrap boostrap,
                                 CommunicationsMonitor monitor) {
        this.url = url;
        this.operationName = operationName;
        this.boostrap = boostrap;
        this.address = address;
        this.headerSerializer = headerSerializer;
        this.inputSerializer = inputSerializer;
        this.monitor = monitor;
    }

    public Message invoke(final Message msg) {
        ChannelFuture future = boostrap.connect(address);
        future.addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture future) throws Exception {
                Channel channel = future.getChannel();
                if (!future.isSuccess()) {
                    monitor.error(future.getCause());
                    return;
                }
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
                            throw new UnsupportedOperationException("Multiple paramters not supported");
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
                writeFuture.addListener(new ChannelFutureListener() {

                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            monitor.error(future.getCause());
                            return;
                        }
                        future.getChannel().close();

                    }
                });
            }
        });
        return MESSAGE;
    }

    public void setNext(Interceptor next) {
        throw new IllegalArgumentException("Interceptor must be the last in the chain");
    }

    public Interceptor getNext() {
        return null;
    }
}
