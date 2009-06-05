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

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.net.NetBindingMonitor;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
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
    private MessageEncoder messageEncoder;
    private ParameterEncoder parameterEncoder;
    private ClientBootstrap bootstrap;
    private SocketAddress address;
    private int retries;
    private NetBindingMonitor monitor;
    private String url;

    /**
     * Constructor.
     *
     * @param url              the target service URL
     * @param operationName    the name of the operation being invoked
     * @param address          the target service address
     * @param messageEncoder   encoder for message envelopers
     * @param parameterEncoder encoder for parameters
     * @param bootstrap        the Netty ClientBootstrap instance for sending invocations
     * @param retries          the number of times to retry failed communications operations
     * @param monitor          the event monitor
     */
    public HttpOneWayInterceptor(String url,
                                 String operationName,
                                 SocketAddress address,
                                 MessageEncoder messageEncoder,
                                 ParameterEncoder parameterEncoder,
                                 ClientBootstrap bootstrap,
                                 int retries,
                                 NetBindingMonitor monitor) {
        this.url = url;
        this.operationName = operationName;
        this.messageEncoder = messageEncoder;
        this.parameterEncoder = parameterEncoder;
        this.bootstrap = bootstrap;
        this.address = address;
        this.retries = retries;
        this.monitor = monitor;
    }

    public Message invoke(final Message msg) {
        try {
            // connection succeeded, write data
            HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, url);
            String serialized = parameterEncoder.encodeText(msg);
            if (msg.isFault()) {
                msg.setBodyWithFault(serialized);
            } else {
                msg.setBody(serialized);
            }
            HttpCallback callback = new HttpCallback(request);
            String encodedMessage = messageEncoder.encodeText(operationName, msg, callback);
            ChannelFuture future = bootstrap.connect(address);
            HttpRetryConnectListener listener = new HttpRetryConnectListener(request, encodedMessage, address, bootstrap, retries, monitor);
            future.addListener(listener);
            return MESSAGE;
        } catch (EncoderException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public void setNext(Interceptor next) {
        throw new IllegalArgumentException("Interceptor must be the last in the chain");
    }

    public Interceptor getNext() {
        return null;
    }
}
