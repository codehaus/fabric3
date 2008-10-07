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
package org.fabric3.federation.shoal;

import com.sun.enterprise.ee.cms.core.Action;
import com.sun.enterprise.ee.cms.core.ActionException;
import com.sun.enterprise.ee.cms.core.MessageActionFactory;
import com.sun.enterprise.ee.cms.core.Signal;
import com.sun.enterprise.ee.cms.core.SignalAcquireException;
import com.sun.enterprise.ee.cms.core.SignalReleaseException;
import com.sun.enterprise.ee.cms.core.MessageAction;

/**
 * Dispatches to a callback handler.
 *
 * @version $Revision$ $Date$
 */
public class DispatchingMessageActionFactory implements MessageActionFactory {
    private String serviceName;
    private final FederationCallback callback;
    private FederationServiceMonitor monitor;

    public DispatchingMessageActionFactory(String serviceName, FederationCallback callback, FederationServiceMonitor monitor) {
        this.serviceName = serviceName;
        this.callback = callback;
        this.monitor = monitor;
    }

    public Action produceAction() {
        return new DispatchingMessageAction();
    }

    private class DispatchingMessageAction implements MessageAction {

        public void consumeSignal(Signal signal) throws ActionException {
            try {
                signal.acquire();
                callback.onSignal(signal);
            } catch (SignalAcquireException e) {
                monitor.onException("Error acquiring signal", serviceName, e);
            } catch (FederationCallbackException e) {
                monitor.onException("Error processing signal", serviceName, e);
            } finally {
                try {
                    signal.release();
                } catch (SignalReleaseException e) {
                    monitor.onException("Error releasing signal", serviceName, e);
                }
            }
        }
    }
}
