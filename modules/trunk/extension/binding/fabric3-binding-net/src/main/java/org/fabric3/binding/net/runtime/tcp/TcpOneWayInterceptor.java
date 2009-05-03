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

import java.net.SocketAddress;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import org.fabric3.binding.net.provision.NetConstants;
import org.fabric3.binding.net.runtime.CommunicationsMonitor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.services.serializer.Serializer;
import org.fabric3.spi.wire.Interceptor;

/**
 * Propagates non-blocking invocations made by a client over a TCP channel. This interceptor is placed on the reference side of an invocation chain.
 *
 * @version $Revision$ $Date$
 */
public class TcpOneWayInterceptor implements Interceptor {
    private static final Message MESSAGE = new MessageImpl();
    private String targetUri;
    private ClientBootstrap boostrap;
    private SocketAddress address;
    private Serializer serializer;
    private CommunicationsMonitor monitor;
    private String operationName;

    /**
     * Constructor.
     *
     * @param targetUri     the target service URI
     * @param operationName the name of the operation being invoked
     * @param address       the target service address
     * @param serializer    serializes the invocation message
     * @param boostrap      the Netty ClientBootstrap instance for sending invocations
     * @param monitor       the event monitor
     */
    public TcpOneWayInterceptor(String targetUri,
                                String operationName,
                                SocketAddress address,
                                Serializer serializer,
                                ClientBootstrap boostrap,
                                CommunicationsMonitor monitor) {
        this.operationName = operationName;
        this.targetUri = targetUri;
        this.boostrap = boostrap;
        this.address = address;
        this.serializer = serializer;
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
                WorkContext workContext = msg.getWorkContext();
                // set the target uri and operation names
                workContext.setHeader(NetConstants.TARGET_URI, targetUri);
                workContext.setHeader(NetConstants.OPERATION_NAME, operationName);

                byte[] serialized = serializer.serialize(byte[].class, msg);

                ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(serialized);
                ChannelFuture writeFuture = channel.write(buffer);
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