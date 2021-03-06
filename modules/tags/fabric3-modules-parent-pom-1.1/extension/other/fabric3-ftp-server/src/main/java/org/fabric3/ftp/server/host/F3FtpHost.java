/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.ftp.server.host;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.work.WorkScheduler;

/**
 * F3 implementation of the in-process FTP host.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class F3FtpHost implements FtpHost {
    private FtpHostMonitor monitor;
    private WorkScheduler workScheduler;
    private int commandPort = 2000;
    private SocketAcceptor acceptor;
    private IoHandler ftpHandler;
    private ProtocolCodecFactory codecFactory;
    private String listenAddress;
    private int idleTimeout = 60; // 60 seconds default

    /**
     * Starts the FTP server.
     *
     * @throws IOException If unable to start the FTP server.
     */
    @Init
    public void start() throws IOException {
        InetSocketAddress socketAddress;
        if (listenAddress == null) {
            socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), commandPort);
        } else {
            socketAddress = new InetSocketAddress(listenAddress, commandPort);
        }
        acceptor = new NioSocketAcceptor();
        SocketSessionConfig config = acceptor.getSessionConfig();
        config.setIdleTime(IdleStatus.BOTH_IDLE, idleTimeout);
        acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(workScheduler));
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));
        acceptor.setHandler(ftpHandler);
        monitor.extensionStarted();
        acceptor.bind(socketAddress);
        monitor.startFtpListener(commandPort);
    }

    /**
     * Stops the FTP server.
     */
    @Destroy
    public void stop() {
        acceptor.unbind();
        acceptor.dispose();
        monitor.extensionStopped();
    }

    /**
     * Sets the monitor.
     *
     * @param monitor the monitor.
     */
    @Monitor
    public void setMonitor(FtpHostMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Sets the handler for the FTP commands.
     *
     * @param ftpHandler FTP Handler.
     */
    @Reference
    public void setFtpHandler(IoHandler ftpHandler) {
        this.ftpHandler = ftpHandler;
    }

    /**
     * Sets the protocol codec factory.
     *
     * @param codecFactory Protocol codec.
     */
    @Reference
    public void setCodecFactory(ProtocolCodecFactory codecFactory) {
        this.codecFactory = codecFactory;
    }

    /**
     * Sets the work scheduler for task execution.
     *
     * @param workScheduler the scheduler
     */
    @Reference
    public void setWorkScheduler(WorkScheduler workScheduler) {
        this.workScheduler = workScheduler;
    }

    /**
     * Sets the FTP command port.
     *
     * @param commandPort Command port.
     */
    @Property
    public void setCommandPort(int commandPort) {
        this.commandPort = commandPort;
    }

    /**
     * Sets the optional timeout in milliseconds for sockets that are idle.
     *
     * @param timeout timeout in milliseconds.
     */
    @Property
    public void setIdleTimeout(int timeout) {
        this.idleTimeout = timeout / 1000;   // convert to seconds used by Mina
    }

    /**
     * Sets the address the server should bind to. This is used for multi-homed machines.
     *
     * @param listenAddress the address to bind to
     */
    @Property
    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }
}
