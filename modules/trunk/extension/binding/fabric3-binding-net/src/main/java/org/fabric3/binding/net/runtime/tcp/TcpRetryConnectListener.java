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

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import org.fabric3.binding.net.provision.NetConstants;
import org.fabric3.binding.net.runtime.CommunicationsMonitor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.binding.serializer.Serializer;

/**
 * Listens for a channel connection event for a TCP socket, retrying a specified number of times if the operation failed.
 *
 * @version $Revision$ $Date$
 */
public class TcpRetryConnectListener implements ChannelFutureListener {
    private Message msg;
    private String targetUri;
    private SocketAddress address;
    private String operationName;
    private Serializer serializer;
    private ClientBootstrap bootstrap;
    private int maxRetry;
    private CommunicationsMonitor monitor;
    private AtomicInteger retryCount;

    public TcpRetryConnectListener(Message msg,
                                   String targetUri,
                                   SocketAddress address,
                                   String operationName,
                                   Serializer serializer,
                                   ClientBootstrap bootstrap,
                                   int maxRetry,
                                   CommunicationsMonitor monitor) {
        this.msg = msg;
        this.targetUri = targetUri;
        this.address = address;
        this.operationName = operationName;
        this.serializer = serializer;
        this.bootstrap = bootstrap;
        this.maxRetry = maxRetry;
        this.monitor = monitor;
        retryCount = new AtomicInteger(0);
    }

    public void operationComplete(ChannelFuture future) throws Exception {
        Channel channel = future.getChannel();
        if (!future.isSuccess() && retryCount.getAndIncrement() >= maxRetry) {
            // connection failed and max number of retries exceeded
            monitor.error(future.getCause());
            return;
        }

        if (!future.isSuccess()) {
            // retry the connection
            ChannelFuture openFuture = bootstrap.connect(address);
            openFuture.addListener(this);
            return;
        }
        // connection succeeded, write data

        WorkContext workContext = msg.getWorkContext();

        // set the target uri and operation names
        workContext.setHeader(NetConstants.TARGET_URI, targetUri);
        workContext.setHeader(NetConstants.OPERATION_NAME, operationName);

        byte[] serialized = serializer.serialize(byte[].class, msg);

        ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(serialized);
        ChannelFuture writeFuture = channel.write(buffer);
        writeFuture.addListener(new TcpRetryWriteListener(buffer, maxRetry, monitor));

    }

}