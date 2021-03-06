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
package org.fabric3.fabric.instantiator.target;

import java.net.URI;

import org.fabric3.host.domain.AssemblyFailure;

/**
 * Thrown when an attempt is made to wire a reference to a service with incompatible contracts.
 *
 * @version $Revision$ $Date$
 */
public class IncompatibleContracts extends AssemblyFailure {
    private URI referenceUri;
    private URI serviceUri;

    /**
     * Constructor.
     *
     * @param referenceUri    the URI of the reference
     * @param serviceUri      the URI of the service
     * @param componentUri    the URI of the component associated with the failure.
     * @param contributionUri the contribution containing the component
     */
    public IncompatibleContracts(URI referenceUri, URI serviceUri, URI componentUri, URI contributionUri) {
        super(componentUri, contributionUri);
        this.referenceUri = referenceUri;
        this.serviceUri = serviceUri;
    }

    public String getMessage() {
        return "The contracts for the reference " + referenceUri + " and service " + serviceUri + " are incompatible";
    }
}
