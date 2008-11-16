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
package org.fabric3.spi.policy;

import java.util.List;

import javax.xml.namespace.QName;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;

/**
 * @version $Revision$ $Date$
 */
public interface Policy {

    /**
     * Intents that are provided by the binding or implemenenation for the
     * requested operation.
     * 
     * @param operation Operation for which requested intents are provided.
     * @return Requested intents that are provided.
     */
    public List<QName> getProvidedIntents(Operation<?> operation);

    /**
     * Policy sets that are provided by the binding or implemenenation for the
     * requested operation.
     * 
     * @param operation Operation for which requested intents are provided.
     * @return Resolved policy sets that are provided.
     */
    public List<PolicySet> getProvidedPolicySets(Operation<?> operation);

    /**
     * Intents that are provided by the binding or implemenenation for 
     * all operations.
     * 
     * @return Requested intents that are provided.
     */
    public List<QName> getProvidedIntents();

    /**
     * Policy sets that are provided by the binding or implemenenation for 
     * all operations.
     * 
     * @return Resolved policy sets that are provided.
     */
    public List<PolicySet> getProvidedPolicySets();

}
