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
package org.fabric3.binding.net.runtime.http;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.HttpRequest;

import org.fabric3.binding.net.runtime.CommunicationsMonitor;

/**
 * Listens for the completion of an HTTP channel write operation and retries the specified number of times if the operation failed.
 *
 * @version $Revision$ $Date$
 */
public class HttpRetryWriteListener implements ChannelFutureListener {
    private HttpRequest request;
    private CommunicationsMonitor monitor;
    private int maxRetry;
    private AtomicInteger retryCount;

    /**
     * Constructor.
     *
     * @param request  the HTTP request being written
     * @param maxRetry the maximum number of times to retry on failure
     * @param monitor  the communications monitor
     */
    public HttpRetryWriteListener(HttpRequest request, int maxRetry, CommunicationsMonitor monitor) {
        this.request = request;
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
        ChannelFuture writeFuture = future.getChannel().write(request);
        writeFuture.addListener(this);
    }
}
