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

import org.fabric3.binding.net.NetBindingMonitor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.binding.serializer.Serializer;
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
    private ClientBootstrap bootstrap;
    private SocketAddress address;
    private Serializer headerSerializer;
    private Serializer inputSerializer;
    private int retries;
    private NetBindingMonitor monitor;
    private String url;

    /**
     * Constructor.
     *
     * @param url              the target service URL
     * @param operationName    the name of the operation being invoked
     * @param address          the target service address
     * @param headerSerializer serializes header information
     * @param inputSerializer  serializes input parameters
     * @param bootstrap        the Netty ClientBootstrap instance for sending invocations
     * @param retries          the number of times to retry failed communications operations
     * @param monitor          the event monitor
     */
    public HttpOneWayInterceptor(String url,
                                 String operationName,
                                 SocketAddress address,
                                 Serializer headerSerializer,
                                 Serializer inputSerializer,
                                 ClientBootstrap bootstrap,
                                 int retries,
                                 NetBindingMonitor monitor) {
        this.url = url;
        this.operationName = operationName;
        this.bootstrap = bootstrap;
        this.address = address;
        this.headerSerializer = headerSerializer;
        this.inputSerializer = inputSerializer;
        this.retries = retries;
        this.monitor = monitor;
    }

    public Message invoke(final Message msg) {
        ChannelFuture future = bootstrap.connect(address);
        HttpRetryConnectListener listener = new HttpRetryConnectListener(msg,
                                                                         url,
                                                                         address,
                                                                         operationName,
                                                                         headerSerializer,
                                                                         inputSerializer,
                                                                         bootstrap,
                                                                         retries,
                                                                         monitor);
        future.addListener(listener);
        return MESSAGE;
    }

    public void setNext(Interceptor next) {
        throw new IllegalArgumentException("Interceptor must be the last in the chain");
    }

    public Interceptor getNext() {
        return null;
    }
}
