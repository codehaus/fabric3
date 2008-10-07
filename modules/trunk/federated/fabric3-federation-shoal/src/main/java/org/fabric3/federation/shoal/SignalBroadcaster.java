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

import java.util.Collection;

import com.sun.enterprise.ee.cms.core.CallBack;
import com.sun.enterprise.ee.cms.core.Signal;

/**
 * Broadcasts signals to a set of FederationCallback instances.
 *
 * @version $Revision$ $Date$
 */
public class SignalBroadcaster implements CallBack {
    private FederationServiceMonitor monitor;
    private Collection<FederationCallback> callbacks;

    public SignalBroadcaster(Collection<FederationCallback> callbacks, FederationServiceMonitor monitor) {
        this.callbacks = callbacks;
        this.monitor = monitor;
    }

    public void processNotification(Signal signal) {
        try {
            for (FederationCallback callback : callbacks) {
                callback.onSignal(signal);
            }
        } catch (FederationCallbackException e) {
            monitor.onException("Error processing signal", e);
        }


    }
}
