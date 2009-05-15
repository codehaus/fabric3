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
package org.fabric3.binding.net.runtime.tcp;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;

import org.fabric3.binding.net.provision.NetConstants;
import org.fabric3.spi.binding.format.EncodeCallback;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;

/**
 * Makes a blocking request-response style invocation over the TCP channel. This interceptor is placed on the reference side of an invocation chain.
 *
 * @version $Revision$ $Date$
 */
public class TcpRequestResponseInterceptor implements Interceptor {
    private static final EncodeCallback CALLBACK = new TcpRequestResponseCallback();
    private String operationName;
    private MessageEncoder messageEncoder;
    private ParameterEncoder parameterEncoder;
    private ClientBootstrap boostrap;
    private SocketAddress address;
    private int maxRetry;
    private String path;
    private AtomicInteger retryCount;

    /**
     * Constructor.
     *
     * @param path             the path part of the target service URI
     * @param operationName    the name of the operation being invoked
     * @param messageEncoder   encodes the message envelope
     * @param parameterEncoder encodes parameter data
     * @param address          the target service address
     * @param boostrap         the Netty ClientBootstrap instance for sending invocations
     * @param maxRetry         the number of times to retry an operation
     */
    public TcpRequestResponseInterceptor(String path,
                                         String operationName,
                                         MessageEncoder messageEncoder,
                                         ParameterEncoder parameterEncoder,
                                         SocketAddress address,
                                         ClientBootstrap boostrap,
                                         int maxRetry) {
        this.path = path;
        // TODO support name mangling
        this.operationName = operationName;
        this.messageEncoder = messageEncoder;
        this.parameterEncoder = parameterEncoder;
        this.boostrap = boostrap;
        this.address = address;
        this.maxRetry = maxRetry;
        this.retryCount = new AtomicInteger(0);
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public Message invoke(final Message msg) {
        Channel channel;
        while (true) {
            ChannelFuture future = boostrap.connect(address);
            future.awaitUninterruptibly();
            channel = future.getChannel();
            if (future.isSuccess()) {
                break;
            } else if (!future.isSuccess() && retryCount.getAndIncrement() >= maxRetry) {
                throw new ServiceUnavailableException("Error connecting to path:" + path, future.getCause());
            }
        }

        try {
            WorkContext workContext = msg.getWorkContext();

            // set the target uri and operation names
            workContext.setHeader(NetConstants.TARGET_URI, path);
            workContext.setHeader(NetConstants.OPERATION_NAME, operationName);

            byte[] serialized = parameterEncoder.encodeBytes(msg);
            msg.setBody(serialized);
            byte[] serializedMessage = messageEncoder.encodeBytes(operationName, msg, CALLBACK);
            int size = serializedMessage.length;

            ChannelBufferFactory bufferFactory = channel.getConfig().getBufferFactory();
            ChannelBuffer dynamicBuffer = ChannelBuffers.dynamicBuffer(size, bufferFactory);
            ChannelBufferOutputStream bout = new ChannelBufferOutputStream(dynamicBuffer);
            // write the length of the stream
            bout.writeInt(size);
            // write contents to the buffer
            bout.write(serializedMessage);
            ChannelBuffer buffer = bout.buffer();
            // write to the channel
            channel.write(buffer);

            // retrieve the last handler and block on the response
            TcpResponseHandler handler = (TcpResponseHandler) channel.getPipeline().getLast();
            Message response = handler.getResponse();

            // close the channel
            channel.close();
            return response;
        } catch (EncoderException e) {
            throw new ServiceUnavailableException(e);
        } catch (IOException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public void setNext(Interceptor next) {
        throw new IllegalArgumentException("Interceptor must be the last in the chain");
    }

    public Interceptor getNext() {
        return null;
    }

    private static class TcpRequestResponseCallback implements EncodeCallback {

        public void encodeContentLengthHeader(long length) {
            // no-op
        }

        public void encodeOperationHeader(String name) {
            // no-op
        }

        public void encodeRoutingHeader(String header) {
            // no-op
        }

        public void encodeRoutingHeader(byte[] header) {
            // no-op
        }

    }

}