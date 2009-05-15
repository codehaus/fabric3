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
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;

import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.wire.Interceptor;

/**
 * Makes a blocking request-response style invocation over an HTTP channel. This interceptor is placed on the reference side of an invocation chain.
 *
 * @version $Revision$ $Date$
 */
public class HttpRequestResponseInterceptor implements Interceptor {
    private String operationName;
    private ParameterEncoder parameterEncoder;
    private ClientBootstrap boostrap;
    private SocketAddress address;
    private int maxRetry;
    private String path;

    private AtomicInteger retryCount;
    private MessageEncoder messageEncoder;

    /**
     * Constructor.
     *
     * @param path             the path part of the target service URI
     * @param operationName    the name of the operation being invoked
     * @param messageEncoder   the message envelope encoder
     * @param parameterEncoder the parameter encoder
     * @param address          the target service address
     * @param boostrap         the Netty ClientBootstrap instance for sending invocations
     * @param maxRetry         the number of times to retry an operation
     */
    public HttpRequestResponseInterceptor(String path,
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

        try {
            String serialized = parameterEncoder.encodeText(msg);
            if (msg.isFault()) {
                msg.setBodyWithFault(serialized);
            } else {
                msg.setBody(serialized);
            }

            HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, path);
            HttpCallback callback = new HttpCallback(request);
            String message = messageEncoder.encodeText(operationName, msg, callback);
            ChannelBuffer buf = ChannelBuffers.copiedBuffer(message, "UTF-8");
            request.setContent(buf);

            channel.write(request);
        } catch (EncoderException e) {
            throw new ServiceRuntimeException(e);
        }


        HttpResponseHandler handler = (HttpResponseHandler) channel.getPipeline().getLast();

        // block on the response
        Response response = handler.getResponse();

        channel.close();
        if (response.getCode() >= 400) {
            try {
                Message ret = messageEncoder.decodeFault(response.getContent());
                Throwable deserialized = parameterEncoder.decodeFault(operationName, (String) ret.getBody());
                ret.setBodyWithFault(deserialized);
                return ret;
            } catch (EncoderException e) {
                throw new ServiceRuntimeException(e);
            }
        } else {
            try {
                Message ret = messageEncoder.decodeResponse(response.getContent());
                Object deserialized = parameterEncoder.decodeResponse(operationName, (String) ret.getBody());
                ret.setBody(deserialized);
                return ret;
            } catch (EncoderException e) {
                throw new ServiceRuntimeException(e);
            }
        }
    }

    public void setNext(Interceptor next) {
        throw new IllegalArgumentException("Interceptor must be the last in the chain");
    }

    public Interceptor getNext() {
        return null;
    }


}