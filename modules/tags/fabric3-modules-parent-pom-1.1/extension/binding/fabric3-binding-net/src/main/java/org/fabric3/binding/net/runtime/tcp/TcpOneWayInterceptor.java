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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.net.NetBindingMonitor;
import org.fabric3.binding.net.provision.NetConstants;
import org.fabric3.spi.binding.format.EncodeCallback;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;

/**
 * Propagates non-blocking invocations made by a client over a TCP channel. This interceptor is placed on the reference side of an invocation chain.
 *
 * @version $Revision$ $Date$
 */
public class TcpOneWayInterceptor implements Interceptor {
    private static final Message MESSAGE = new MessageImpl();
    private static final EncodeCallback CALLBACK = new TcpOneWayCallback();
    private String targetUri;
    private MessageEncoder messageEncoder;
    private ClientBootstrap boostrap;
    private SocketAddress address;
    private ParameterEncoder parameterEncoder;
    private NetBindingMonitor monitor;
    private String operationName;
    private int maxRetry;

    /**
     * Constructor.
     *
     * @param targetUri        the target service URI
     * @param operationName    the name of the operation being invoked
     * @param address          the target service address
     * @param messageEncoder   encodes the message envelope
     * @param parameterEncoder encodes the invocation paramters
     * @param boostrap         the Netty ClientBootstrap instance for sending invocations
     * @param maxRetry         the number of times to retry an operation
     * @param monitor          the event monitor
     */
    public TcpOneWayInterceptor(String targetUri,
                                String operationName,
                                SocketAddress address,
                                MessageEncoder messageEncoder,
                                ParameterEncoder parameterEncoder,
                                ClientBootstrap boostrap,
                                int maxRetry,
                                NetBindingMonitor monitor) {
        this.operationName = operationName;
        this.targetUri = targetUri;
        this.messageEncoder = messageEncoder;
        this.boostrap = boostrap;
        this.address = address;
        this.parameterEncoder = parameterEncoder;
        this.maxRetry = maxRetry;
        this.monitor = monitor;
    }

    public Message invoke(Message msg) {
        // Copy the work context since the binding write operation is performed asynchronously in a different thread and may occur after this
        // invocation has returned. Copying avoids the possibility of another operation modifying the work context before it is accessed by
        // this write.
        WorkContext oldWorkContext = msg.getWorkContext();
        List<CallFrame> newStack = null;
        List<CallFrame> stack = oldWorkContext.getCallFrameStack();
        if (stack != null && !stack.isEmpty()) {
            // clone the callstack to avoid multiple threads seeing changes
            newStack = new ArrayList<CallFrame>(stack);
        }
        msg.setWorkContext(null);
        Map<String, Object> newHeaders = null;
        Map<String, Object> headers = oldWorkContext.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            // clone the headers to avoid multiple threads seeing changes
            newHeaders = new HashMap<String, Object>(headers);
        }
        WorkContext context = new WorkContext();
        context.addCallFrames(newStack);
        context.addHeaders(newHeaders);
        msg.setWorkContext(context);

        // set the target uri and operation names
        context.setHeader(NetConstants.TARGET_URI, targetUri);
        context.setHeader(NetConstants.OPERATION_NAME, operationName);

        try {
            byte[] serialized = parameterEncoder.encodeBytes(msg);
            if (msg.isFault()) {
                msg.setBodyWithFault(serialized);
            } else {
                msg.setBody(serialized);
            }
            byte[] serializedMessage = messageEncoder.encodeBytes(operationName, msg, CALLBACK);
            TcpRetryConnectListener listener = new TcpRetryConnectListener(serializedMessage, address, boostrap, maxRetry, monitor);
            ChannelFuture future = boostrap.connect(address);
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

    private static class TcpOneWayCallback implements EncodeCallback {

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