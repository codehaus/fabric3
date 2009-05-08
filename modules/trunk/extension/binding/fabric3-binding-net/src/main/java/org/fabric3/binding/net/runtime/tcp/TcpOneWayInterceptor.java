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
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;

import org.fabric3.binding.net.NetBindingMonitor;
import org.fabric3.spi.binding.serializer.Serializer;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.CallFrame;
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
    private NetBindingMonitor monitor;
    private String operationName;
    private int maxRetry;

    /**
     * Constructor.
     *
     * @param targetUri     the target service URI
     * @param operationName the name of the operation being invoked
     * @param address       the target service address
     * @param serializer    serializes the invocation message
     * @param boostrap      the Netty ClientBootstrap instance for sending invocations
     * @param maxRetry      the number of times to retry an operation
     * @param monitor       the event monitor
     */
    public TcpOneWayInterceptor(String targetUri,
                                String operationName,
                                SocketAddress address,
                                Serializer serializer,
                                ClientBootstrap boostrap,
                                int maxRetry,
                                NetBindingMonitor monitor) {
        this.operationName = operationName;
        this.targetUri = targetUri;
        this.boostrap = boostrap;
        this.address = address;
        this.serializer = serializer;
        this.maxRetry = maxRetry;
        this.monitor = monitor;
    }

    public Message invoke(Message msg) {
        ChannelFuture future = boostrap.connect(address);
        // Copy the work context since the binding write operation is performed asynchronously in a different thread and may occur after this
        // invocation has returned. Copying avoids the possibility of another operation modifying the work context before it is accessed by
        // this write.
        WorkContext workContext = msg.getWorkContext();
        List<CallFrame> newStack = null;
        List<CallFrame> stack = workContext.getCallFrameStack();
        if (stack != null && !stack.isEmpty()) {
            // clone the callstack to avoid multiple threads seeing changes
            newStack = new ArrayList<CallFrame>(stack);
        }
        msg.setWorkContext(null);
        Map<String, Object> newHeaders = null;
        Map<String, Object> headers = workContext.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            // clone the headers to avoid multiple threads seeing changes
            newHeaders = new HashMap<String, Object>(headers);
        }
        WorkContext context = new WorkContext();
        context.addCallFrames(newStack);
        context.addHeaders(newHeaders);
        msg.setWorkContext(context);
        TcpRetryConnectListener listener =
                new TcpRetryConnectListener(msg, targetUri, address, operationName, serializer, boostrap, maxRetry, monitor);
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