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

import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import static org.jboss.netty.channel.Channels.pipeline;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.handler.timeout.Timer;
import org.jboss.netty.handler.timeout.WriteTimeoutHandler;

/**
 * Creates a TCP channel pipeline for both clients and service providers.
 *
 * @version $Revision$ $Date$
 */
public class TcpPipelineFactory implements ChannelPipelineFactory {
    private final ChannelHandler handler;
    private Timer timer;
    private long timeout;

    public TcpPipelineFactory(ChannelHandler handler, Timer timer, long timeout) {
        this.handler = handler;
        this.timer = timer;
        this.timeout = timeout;
    }

    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();
        int secondsTimeout = (int) (timeout / 10);
        pipeline.addLast("idlehandler", new IdleStateHandler(timer, secondsTimeout, secondsTimeout, secondsTimeout));
        pipeline.addLast("readTimeout", new ReadTimeoutHandler(timer, timeout, TimeUnit.MILLISECONDS));
        pipeline.addLast("writeTimeout", new WriteTimeoutHandler(timer, timeout, TimeUnit.MILLISECONDS));
        pipeline.addLast("handler", handler);
        return pipeline;
    }
}