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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.CLOSE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import org.osoa.sca.Conversation;

import org.fabric3.binding.net.provision.NetConstants;
import static org.fabric3.binding.net.provision.NetConstants.OPERATION_NAME;
import org.fabric3.binding.net.runtime.CommunicationsMonitor;
import org.fabric3.binding.net.runtime.NetRequestHandler;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.services.serializer.SerializationException;
import org.fabric3.spi.services.serializer.Serializer;
import org.fabric3.spi.util.Base64;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Handles incoming requests from an HTTP channel. This is placed on the service side of an invocation chain.
 */
@ChannelPipelineCoverage("one")
public class HttpRequestHandler extends SimpleChannelHandler implements NetRequestHandler {
    private Serializer serializer;
    private CommunicationsMonitor monitor;
    private String contentType = "text/plain; charset=UTF-8";
    private volatile HttpRequest request;
    private volatile boolean readingChunks;
    private Map<String, Holder> wires = new ConcurrentHashMap<String, Holder>();
    private StringBuilder requestContent = new StringBuilder();

    public HttpRequestHandler(Serializer serializer, CommunicationsMonitor monitor) {
        this.serializer = serializer;
        this.monitor = monitor;
    }

    public void register(String path, String callbackUri, Wire wire) {
        Holder holder = new Holder(callbackUri, wire);
        wires.put(path, holder);
    }

    public void unregister(String path) {
        wires.remove(path);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
        if (!readingChunks) {
            HttpRequest request = this.request = (HttpRequest) event.getMessage();
            if (request.isChunked()) {
                readingChunks = true;
            } else {
                ChannelBuffer content = request.getContent();
                invoke(request, content.toString("UTF-8"), event);
                // TODO handle exceptions
            }
        } else {
            HttpChunk chunk = (HttpChunk) event.getMessage();
            if (chunk.isLast()) {
                // end of content
                readingChunks = false;
                requestContent.append(chunk.getContent().toString("UTF-8"));
                invoke(request, requestContent.toString(), event);
                // TODO handle exceptions
            } else {
                requestContent.append(chunk.getContent().toString("UTF-8"));
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private void invoke(HttpRequest request, String content, MessageEvent event) throws SerializationException, ClassNotFoundException {
        Holder holder = wires.get(request.getUri());
        if (holder == null) {
            throw new AssertionError("Holder not found for request:" + request.getUri());
        }
        Wire wire = holder.getWire();
        String callbackUri = holder.getCallbackUri();
        String routing = request.getHeader(NetConstants.ROUTING);
        Message message = new MessageImpl();
        WorkContext context = new WorkContext();
        message.setWorkContext(context);
        if (routing != null) {
            List<CallFrame> frames = (List<CallFrame>) serializer.deserialize(Object.class, Base64.decode(routing));
            context.addCallFrames(frames);
            CallFrame previous = context.peekCallFrame();
            // Copy correlation and conversation information from incoming frame to new frame
            // Note that the callback URI is set to the callback address of this service so its callback wire can be mapped in the case of a
            // bidirectional service
            Object id = previous.getCorrelationId(Object.class);
            ConversationContext conversationContext = previous.getConversationContext();
            Conversation conversation = previous.getConversation();
            CallFrame frame = new CallFrame(callbackUri, id, conversation, conversationContext);
            context.addCallFrame(frame);
        }
        message.setBody(content);
        Interceptor interceptor = selectOperation(request, wire);
        Message response = interceptor.invoke(message);
        writeResponse(event, response);
    }

    private Interceptor selectOperation(HttpRequest request, Wire wire) {
        if (wire.getInvocationChains().size() == 1) {
            // only one operation, select it
            return wire.getInvocationChains().values().iterator().next().getHeadInterceptor();
        }
        String operationName = request.getHeader(OPERATION_NAME);
        if (operationName != null) {
            InvocationChain chain = null;
            for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
                PhysicalOperationDefinition definition = entry.getKey();
                if (definition.getName().equals(operationName)) {
                    chain = entry.getValue();
                }
            }
            if (chain != null) {
                return chain.getHeadInterceptor();
            }
        }
        // TODO should select from HTTP Method name
        throw new AssertionError();
    }


    private void writeResponse(MessageEvent event, Message msg) {
        // reuse buffer
        requestContent.setLength(0);

        // Determine if the connection should be closed
        String header = request.getHeader(CONNECTION);
        boolean close = CLOSE.equalsIgnoreCase(header) || request.getProtocolVersion().equals(HTTP_1_0) && !KEEP_ALIVE.equalsIgnoreCase(header);

        HttpResponseStatus status;
        if (msg.isFault()) {
            status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        } else {
            status = HttpResponseStatus.OK;
        }
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        String content = (String) msg.getBody();

        if (content != null) {
            ChannelBuffer buf = ChannelBuffers.copiedBuffer(content, "UTF-8");
            response.setContent(buf);
            response.setHeader(CONTENT_LENGTH, String.valueOf(buf.readableBytes()));
        } else {
            // FIXME should not have to do ths but otherwise it thinks the response is chunked
            response.setHeader(CONTENT_LENGTH, "1");
            response.setContent(ChannelBuffers.copiedBuffer(" ", "UTF-8"));
        }
        response.setHeader(CONTENT_TYPE, contentType);

        // Write the response
        ChannelFuture future = event.getChannel().write(response);
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
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
