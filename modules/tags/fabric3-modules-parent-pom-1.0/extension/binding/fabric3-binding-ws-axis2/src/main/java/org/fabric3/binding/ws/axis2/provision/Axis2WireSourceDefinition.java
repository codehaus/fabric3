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
package org.fabric3.binding.ws.axis2.provision;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 * @version $Revision$ $Date$
 */
public class Axis2WireSourceDefinition extends PhysicalWireSourceDefinition implements Axis2PolicyAware {
	
	private static final long serialVersionUID = -4225747629340608153L;
	
	private String serviceInterface;
    private Map<String, Set<AxisPolicy>> policies = new HashMap<String, Set<AxisPolicy>>();

    /**
     * @return Service interface for the wire source.
     */
    public String getServiceInterface() {
        return serviceInterface;
    }

    /**
     * @param serviceInterface Service interface for the wire source.
     */
    public void setServiceInterface(String serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    /**
     * @return Policy definitions.
     */
    public Set<AxisPolicy> getPolicies(String operation) {
        return policies.get(operation);
    }

    /**
     * @param policy Policy definitions.
     */
    public void addPolicy(String operation, AxisPolicy policy) {

        if (!this.policies.containsKey(operation)) {
            this.policies.put(operation, new HashSet<AxisPolicy>());
        }
        this.policies.get(operation).add(policy);
    }

}
