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
import org.oasisopen.sca.ServiceUnavailableException;

import org.fabric3.binding.net.provision.NetConstants;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.services.serializer.SerializationException;
import org.fabric3.spi.services.serializer.Serializer;
import org.fabric3.spi.wire.Interceptor;

/**
 * Makes a blocking request-response style invocation over the TCP channel. This interceptor is placed on the reference side of an invocation chain.
 *
 * @version $Revision$ $Date$
 */
public class TcpRequestResponseInterceptor implements Interceptor {
    private String operationName;
    private Serializer serializer;
    private ClientBootstrap boostrap;
    private SocketAddress address;
    private String path;

    public TcpRequestResponseInterceptor(String path, String operationName, Serializer serializer, ClientBootstrap boostrap, SocketAddress address) {
        this.path = path;
        // TODO support name mangling
        this.operationName = operationName;
        this.serializer = serializer;
        this.boostrap = boostrap;
        this.address = address;
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public Message invoke(final Message msg) {
        ChannelFuture future = boostrap.connect(address);
        future.awaitUninterruptibly();
        Channel channel = future.getChannel();
        if (!future.isSuccess()) {
            throw new ServiceUnavailableException("Error connecting to path:" + path, future.getCause());
        }
        WorkContext workContext = msg.getWorkContext();

        // set the target uri and operation names
        workContext.setHeader(NetConstants.TARGET_URI, path);
        workContext.setHeader(NetConstants.OPERATION_NAME, operationName);

        byte[] serialized;
        try {
            serialized = serializer.serialize(msg);
        } catch (SerializationException e) {
            throw new ServiceUnavailableException(e);
        }

        ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(serialized);
        channel.write(buffer);

        TcpResponseHandler handler = (TcpResponseHandler) channel.getPipeline().getLast();

        // block on the response
        Message response = handler.getResponse();
        channel.close();
        return response;
    }

    public void setNext(Interceptor next) {
        throw new IllegalArgumentException("Interceptor must be the last in the chain");
    }

    public Interceptor getNext() {
        return null;
    }


}