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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.net.NetBindingMonitor;
import org.fabric3.binding.net.provision.NetConstants;
import org.fabric3.spi.binding.format.HeaderContext;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.Message;

/**
 * Handles TCP responses on the client side for request-response style interactions. This handler is placed on the reference side of an invocation
 * chain.
 */
@ChannelPipelineCoverage("one")
public class TcpResponseHandler extends SimpleChannelHandler {
    private static final HeaderContext CONTEXT = new TcpResponseHeaderContext();
    private MessageEncoder messageEncoder;
    private ParameterEncoder parameterEncoder;
    private long responseWait;
    private NetBindingMonitor monitor;

    // queue used by clients to block on awaiting a response
    private BlockingQueue<Message> responseQueue = new LinkedBlockingQueue<Message>();

    public TcpResponseHandler(MessageEncoder messageEncoder, ParameterEncoder parameterEncoder, long responseWait, NetBindingMonitor monitor) {
        this.messageEncoder = messageEncoder;
        this.parameterEncoder = parameterEncoder;
        this.responseWait = responseWait;
        this.monitor = monitor;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
        if (buffer.readable()) {
            byte[] bytes = buffer.toByteBuffer().array();
            Message message = messageEncoder.decode(bytes, CONTEXT);
            String operationName = message.getWorkContext().getHeader(String.class, NetConstants.OPERATION_NAME);
            if (message.isFault()) {
                Throwable fault = parameterEncoder.decodeFault(operationName, (byte[]) message.getBody());
                message.setBodyWithFault(fault);
            } else {
                Object deserialized = parameterEncoder.decodeResponse(operationName, (byte[]) message.getBody());
                message.setBody(deserialized);
            }
            responseQueue.offer(message);
        }
    }

    /**
     * Blocks on a response.
     *
     * @return the response or null
     * @throws ServiceRuntimeException if waiting on the response times out
     */
    public Message getResponse() throws ServiceRuntimeException {
        try {
            Message response = responseQueue.poll(responseWait, TimeUnit.MILLISECONDS);
            if (response == null) {
                // timed out waiting for a response, throw exception back to the client since this is a blocking operation
                throw new ServiceRuntimeException("Timeout waiting on response");
            }
            return response;
        } catch (InterruptedException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        ctx.getChannel().close();
        monitor.error(e.getCause());
    }

    private static class TcpResponseHeaderContext implements HeaderContext {

        public long getContentLength() {
            throw new UnsupportedOperationException();
        }

        public String getOperationName() {
            throw new UnsupportedOperationException();
        }

        public String getRoutingText() {
            throw new UnsupportedOperationException();
        }

        public byte[] getRoutingBytes() {
            throw new UnsupportedOperationException();
        }
    }

}