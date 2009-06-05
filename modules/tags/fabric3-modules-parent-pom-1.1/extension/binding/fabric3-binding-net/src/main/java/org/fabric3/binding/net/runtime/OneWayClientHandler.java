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
package org.fabric3.binding.net.runtime;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.net.NetBindingMonitor;

/**
 * Client-side handler for one-way operations. This handler only logs errors and closes the channel.
 *
 * @version $Revision$ $Date$
 */
@ChannelPipelineCoverage("all")
public class OneWayClientHandler extends SimpleChannelHandler {
    private NetBindingMonitor monitor;

    public OneWayClientHandler(@Monitor NetBindingMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        monitor.error(e.getCause());
        ctx.getChannel().close();
    }
}
