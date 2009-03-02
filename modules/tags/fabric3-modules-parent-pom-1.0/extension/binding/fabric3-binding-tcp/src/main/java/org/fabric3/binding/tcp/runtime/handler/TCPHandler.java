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
package org.fabric3.binding.tcp.runtime.handler;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.fabric3.binding.tcp.runtime.monitor.TCPBindingMonitor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Wire;

/**
 * Handler to TCP messages delivered.
 * 
 * @version $Revision$ $Date$
 */
public class TCPHandler extends IoHandlerAdapter {
    private Wire wire;
    private TCPBindingMonitor monitor;

    /**
     * Inject wire on TCP Handler
     * 
     * @param wire {@link Wire}
     * @param monitor
     */
    public TCPHandler(Wire wire, TCPBindingMonitor monitor) {
        this.wire = wire;
        this.monitor = monitor;
    }

    /**
     * {@inheritDoc}
     */
    public void messageReceived(IoSession session, Object message) throws Exception {
        Interceptor interceptor = wire.getInvocationChains().values().iterator().next().getHeadInterceptor();
        WorkContext workContext = new WorkContext();
        Message input = new MessageImpl(new Object[] { message }, false, workContext);
        Message msg = interceptor.invoke(input);

        // TODO: Work out if service is of request/response type, and then write
        // the response back.
        if (!msg.isFault() && msg.getBody() != null) {
            session.write(msg.getBody());
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        monitor.onException("Exception caught in TCP binding:TCP handler", cause);
    }

}
