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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
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
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.util.Base64;
import org.fabric3.spi.wire.Interceptor;

/**
 * Handles non-blocking invocations made by a client. This interceptor is placed on the reference side of an invocation chain.
 *
 * @version $Revision$ $Date$
 */
public class HttpOneWayInterceptor implements Interceptor {
    private static final Message MESSAGE = new MessageImpl();
    private String operationName;
    private ClientBootstrap boostrap;
    private SocketAddress address;
    private String url;

    public HttpOneWayInterceptor(String url, String operationName, ClientBootstrap boostrap, SocketAddress address) {
        this.url = url;
        this.operationName = operationName;
        this.boostrap = boostrap;
        this.address = address;
    }

    public Message invoke(final Message msg) {
        ChannelFuture future = boostrap.connect(address);
        future.addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture future) throws Exception {
                Channel channel = future.getChannel();
                if (!future.isSuccess()) {
                    // TODO log error
                }
                HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, url);

                request.addHeader(NetConstants.OPERATION_NAME, operationName);

                List<CallFrame> stack = msg.getWorkContext().getCallFrameStack();
                if (!stack.isEmpty()) {
                    ByteArrayOutputStream bas = new ByteArrayOutputStream();
                    ObjectOutputStream stream = new ObjectOutputStream(bas);
                    stream.writeObject(stack);
                    stream.flush();
                    stream.close();
                    String routing = Base64.encode(bas.toByteArray());
                    request.addHeader(NetConstants.ROUTING, routing);
                }
                Object body = msg.getBody();
                if (body != null) {
                    ChannelBuffer buf = ChannelBuffers.copiedBuffer(body.toString(), "UTF-8");
                    request.addHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(body.toString().length()));
                    request.setContent(buf);
                }
                ChannelFuture writeFuture = channel.write(request);

                writeFuture.addListener(new ChannelFutureListener() {

                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            // TODO log error
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
