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
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;

import org.fabric3.binding.net.provision.NetConstants;
import org.fabric3.spi.binding.serializer.SerializationException;
import org.fabric3.spi.binding.serializer.Serializer;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * Makes a blocking request-response style invocation over an HTTP channel. This interceptor is placed on the reference side of an invocation chain.
 *
 * @version $Revision$ $Date$
 */
public class HttpRequestResponseInterceptor implements Interceptor {
    private String operationName;
    private Serializer headerSerializer;
    private Serializer inputSerializer;
    private Serializer outputSerializer;
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
     * @param headerSerializer serializes header information
     * @param inputSerializer  serializes input parameters
     * @param outputSerializer serializes output parameters
     * @param address          the target service address
     * @param boostrap         the Netty ClientBootstrap instance for sending invocations
     * @param maxRetry         the number of times to retry an operation
     */
    public HttpRequestResponseInterceptor(String path,
                                          String operationName,
                                          Serializer headerSerializer,
                                          Serializer inputSerializer,
                                          Serializer outputSerializer,
                                          SocketAddress address,
                                          ClientBootstrap boostrap,
                                          int maxRetry) {
        this.path = path;
        // TODO support name mangling
        this.operationName = operationName;
        this.headerSerializer = headerSerializer;
        this.inputSerializer = inputSerializer;
        this.outputSerializer = outputSerializer;
        this.boostrap = boostrap;
        this.address = address;
        this.maxRetry = maxRetry;
        retryCount = new AtomicInteger(0);
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

        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, path);

        request.addHeader(NetConstants.OPERATION_NAME, operationName);

        List<CallFrame> stack = msg.getWorkContext().getCallFrameStack();
        if (!stack.isEmpty()) {
            try {
                String serialized = headerSerializer.serialize(String.class, stack);
                request.addHeader(NetConstants.ROUTING, serialized);
            } catch (SerializationException e) {
                // TODO this message is not thrown to the client
                throw new ServiceRuntimeException(e);
            }
        }
        Object body = msg.getBody();
        try {
            String str;
            if (body != null) {
                if (body.getClass().isArray()) {
                    Object[] payload = (Object[]) body;
                    if (payload.length > 1) {
                        throw new UnsupportedOperationException("Multiple paramters not supported");
                    }
                    str = inputSerializer.serialize(String.class, payload[0]);
                } else {
                    str = inputSerializer.serialize(String.class, body);
                }
            } else {
                str = inputSerializer.serialize(String.class, null);
            }
            request.addHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(str.length()));
            ChannelBuffer buf = ChannelBuffers.copiedBuffer(str, "UTF-8");
            request.setContent(buf);
        } catch (SerializationException e) {
            throw new ServiceRuntimeException(e);
        }
        channel.write(request);
        HttpResponseHandler handler = (HttpResponseHandler) channel.getPipeline().getLast();

        // block on the response
        Response response = handler.getResponse();

        channel.close();
        MessageImpl ret = new MessageImpl();
        if (response.getCode() != 200) {
            try {
                Throwable deserialized = outputSerializer.deserializeFault(response.getContent());
                ret.setBodyWithFault(deserialized);
            } catch (SerializationException e) {
                throw new ServiceRuntimeException(e);
            }
        } else {
            try {
                Object deserialized = outputSerializer.deserialize(Object.class, response.getContent());
                ret.setBody(deserialized);
            } catch (SerializationException e) {
                throw new ServiceRuntimeException(e);
            }
        }
        return ret;
    }

    public void setNext(Interceptor next) {
        throw new IllegalArgumentException("Interceptor must be the last in the chain");
    }

    public Interceptor getNext() {
        return null;
    }


}