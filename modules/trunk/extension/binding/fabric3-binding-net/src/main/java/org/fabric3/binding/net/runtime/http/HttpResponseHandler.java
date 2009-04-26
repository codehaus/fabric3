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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.net.runtime.CommunicationsMonitor;

/**
 * Handles HTTP responses on the client side for request-response style interactions. This handler is placed on the reference side of an invocation
 * chain.
 */
@ChannelPipelineCoverage("one")
public class HttpResponseHandler extends SimpleChannelHandler {
    private long responseWait;
    private CommunicationsMonitor monitor;

    private volatile boolean readingChunks;
    private StringBuilder body = new StringBuilder();

    // queue used by clients to block on awaiting a response
    private BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<Response>();

    public HttpResponseHandler(long responseWait, CommunicationsMonitor monitor) {
        this.responseWait = responseWait;
        this.monitor = monitor;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!readingChunks) {
            HttpResponse response = (HttpResponse) e.getMessage();

            if (response.getStatus().getCode() == 200 && response.isChunked()) {
                readingChunks = true;
            } else {
                ChannelBuffer content = response.getContent();
                if (content.readable()) {
                    responseQueue.offer(new Response(response.getStatus().getCode(), content.toString("UTF-8")));
                }
            }
        } else {
            HttpChunk chunk = (HttpChunk) e.getMessage();
            if (chunk.isLast()) {
                readingChunks = false;
                responseQueue.offer(new Response(200, body.toString()));
            } else {
                body.append(chunk.getContent().toString("UTF-8"));
            }
        }
    }

    /**
     * Blocks on a response.
     *
     * @return the response or null
     * @throws ServiceRuntimeException if waiting on the response times out
     */
    public Response getResponse() throws ServiceRuntimeException {
        try {
            Response response = responseQueue.poll(responseWait, TimeUnit.MILLISECONDS);
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

}
