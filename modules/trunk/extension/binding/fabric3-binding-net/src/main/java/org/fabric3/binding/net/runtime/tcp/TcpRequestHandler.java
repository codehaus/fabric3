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

import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import org.fabric3.binding.net.NetBindingMonitor;
import org.fabric3.binding.net.provision.NetConstants;
import org.fabric3.spi.binding.serializer.SerializationException;
import org.fabric3.spi.binding.serializer.Serializer;
import org.fabric3.spi.component.F3Conversation;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Handles incoming requests from a TCP channel. This is placed on the service side of an invocation chain.
 */
@ChannelPipelineCoverage("one")
public class TcpRequestHandler extends SimpleChannelHandler {
    private Serializer serializer;
    private NetBindingMonitor monitor;
    private Map<String, Holder> wires = new ConcurrentHashMap<String, Holder>();
    private long maxObjectSize;

    /**
     * Constructor.
     *
     * @param serializer    serializes messages
     * @param maxObjectSize the maximum object size to handle in bytes. Objects larger than this will be rejected.
     * @param monitor       the event monitor
     */
    public TcpRequestHandler(Serializer serializer, long maxObjectSize, NetBindingMonitor monitor) {
        this.serializer = serializer;
        this.maxObjectSize = maxObjectSize;
        this.monitor = monitor;
    }

    /**
     * Registers a wire for a request path, i.e. the path of the service URI.
     *
     * @param path        the path part of the service URI
     * @param callbackUri the callback URI associated with the wire or null if it is unidirectional
     * @param wire        the wire
     */
    public void register(String path, String callbackUri, Wire wire) {
        Holder holder = new Holder(callbackUri, wire);
        wires.put(path, holder);
    }

    /**
     * Unregisters a wire for a request path, i.e. the path of the service URI.
     *
     * @param path the path part of the service URI
     */
    public void unregister(String path) {
        wires.remove(path);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
        ChannelBuffer buffer = (ChannelBuffer) event.getMessage();
        if (buffer.readableBytes() < 4) {
            return;
        }

        int dataLen = buffer.getInt(buffer.readerIndex());
        if (dataLen <= 0) {
            throw new StreamCorruptedException("Invalid data length: " + dataLen);
        }
        if (dataLen > maxObjectSize) {
            throw new StreamCorruptedException("Exceeded max configured data length: " + dataLen + " Max: " + maxObjectSize);
        }

        if (buffer.readableBytes() < dataLen + 4) {
            return;
        }

        // advance past initial size bytes
        buffer.readBytes(4);

        ChannelBufferInputStream stream = new ChannelBufferInputStream(buffer, dataLen);
        byte[] bytes = new byte[dataLen];
        stream.read(bytes);

        // deserialize the message envelope
        Message msg = serializer.deserializeMessage(bytes);
        WorkContext workContext = msg.getWorkContext();
        String targetUri = workContext.getHeader(String.class, NetConstants.TARGET_URI);
        if (targetUri == null) {
            // programming error
            throw new AssertionError("Target URI not specified in message");
        }
        String operationName = workContext.getHeader(String.class, NetConstants.OPERATION_NAME);
        if (operationName == null) {
            // programming error
            throw new AssertionError("Operation not specified in message");
        }
        Holder holder = wires.get(targetUri);
        if (holder == null) {
            throw new AssertionError("Holder not found for request:" + targetUri);
        }
        Wire wire = holder.getWire();
        String callbackUri = holder.getCallbackUri();
        CallFrame previous = workContext.peekCallFrame();
        // Copy correlation and conversation information from incoming frame to new frame
        // Note that the callback URI is set to the callback address of this service so its callback wire can be mapped in the case of a
        // bidirectional service
        Serializable id = previous.getCorrelationId(Serializable.class);
        ConversationContext conversationContext = previous.getConversationContext();
        F3Conversation conversation = previous.getConversation();
        CallFrame frame = new CallFrame(callbackUri, id, conversation, conversationContext);
        workContext.addCallFrame(frame);

        // deserialize the body
        Object body = msg.getBody();
        if (body != null) {
            Object deserialized = serializer.deserialize(Object.class, body);
            msg.setBody(deserialized);
        }
        Interceptor interceptor = selectOperation(operationName, wire);
        Message response = interceptor.invoke(msg);
        writeResponse(event, response);
    }

    private Interceptor selectOperation(String operationName, Wire wire) {
        InvocationChain chain = null;
        for (InvocationChain invocationChain : wire.getInvocationChains()) {
            PhysicalOperationDefinition definition = invocationChain.getPhysicalOperation();
            if (definition.getName().equals(operationName)) {
                chain = invocationChain;
            }
        }
        if (chain != null) {
            return chain.getHeadInterceptor();
        }
        throw new AssertionError("Invalid operation name: " + operationName);
    }


    private void writeResponse(MessageEvent event, Message msg) throws SerializationException {
        byte[] serialized = serializer.serialize(byte[].class, msg);
        ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(serialized);
        ChannelFuture future = event.getChannel().write(buffer);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        monitor.error(e.getCause());
        e.getChannel().close();
    }

    private class Holder {
        private String callbackUri;
        private Wire wire;

        public Holder(String callbackUri, Wire wire) {
            this.callbackUri = callbackUri;
            this.wire = wire;
        }


        public String getCallbackUri() {
            return callbackUri;
        }

        public Wire getWire() {
            return wire;
        }
    }

}