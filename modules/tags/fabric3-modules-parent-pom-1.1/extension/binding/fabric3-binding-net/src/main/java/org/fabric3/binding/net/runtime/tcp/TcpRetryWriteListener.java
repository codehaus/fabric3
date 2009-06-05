/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ÒLicenseÓ), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an Òas isÓ basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.net.runtime.tcp;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import org.fabric3.binding.net.NetBindingMonitor;

/**
 * Listens for the completion of a channel write operation to a TCP socketand retries the specified number of times if the operation failed.
 *
 * @version $Revision$ $Date$
 */
public class TcpRetryWriteListener implements ChannelFutureListener {
    private ChannelBuffer buffer;
    private NetBindingMonitor monitor;
    private int maxRetry;
    private AtomicInteger retryCount;

    /**
     * Constructor.
     *
     * @param buffer   the request being written
     * @param maxRetry the maximum number of times to retry on failure
     * @param monitor  the communications monitor
     */
    public TcpRetryWriteListener(ChannelBuffer buffer, int maxRetry, NetBindingMonitor monitor) {
        this.buffer = buffer;
        this.monitor = monitor;
        this.maxRetry = maxRetry;
        retryCount = new AtomicInteger(0);
    }

    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            future.getChannel().close();
            return;
        }
        if (!future.isSuccess() && retryCount.getAndIncrement() >= maxRetry) {
            // failed and maximum number of retries exceeded
            monitor.error(future.getCause());
            return;
        }
        // retry the write request
        ChannelFuture writeFuture = future.getChannel().write(buffer);
        writeFuture.addListener(this);
    }
}