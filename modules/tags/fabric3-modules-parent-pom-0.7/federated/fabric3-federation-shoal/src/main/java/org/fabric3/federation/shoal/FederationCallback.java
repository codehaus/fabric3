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

import com.sun.enterprise.ee.cms.core.Signal;

/**
 * Implementations register with the FederationService to receive callbacks for a GroupManagementService instance.
 *
 * @version $Revision$ $Date$
 */
public interface FederationCallback {

    /**
     * Called after the GroupManagementService joins its group.
     *
     * @throws FederationCallbackException if an error occurs performing an after-join operation
     */
    void afterJoin() throws FederationCallbackException;

    /**
     * Called when a signal is dispatched to the GroupManagementService.
     *
     * @param signal the signal
     * @throws FederationCallbackException if an error ocurrs processing the signal
     */
    void onSignal(Signal signal) throws FederationCallbackException;

    /**
     * Called before the GroupManagementService leaves its group.
     *
     * @throws FederationCallbackException if an error occurs performing an operation
     */
    void onLeave() throws FederationCallbackException;

}
