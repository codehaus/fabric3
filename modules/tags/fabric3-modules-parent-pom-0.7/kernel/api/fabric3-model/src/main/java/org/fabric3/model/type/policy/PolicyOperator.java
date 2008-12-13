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
 */
package org.fabric3.model.type.policy;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Generic abstraction for a policy operator.
 *
 */
public class PolicyOperator extends PolicyNode implements PolicyOperatorParent  {
    
    private Set<PolicyOperator> policyOperators = new HashSet<PolicyOperator>();
    private Set<PolicyAssertion> policyAssertions = new HashSet<PolicyAssertion>();
    
    public PolicyOperator(QName qname) {
        super(qname);
    }

    public Set<PolicyAssertion> getPolicyAssertions() {
        return policyAssertions;
    }

    public void addPolicyAssertion(PolicyAssertion policyAssertion) {
        policyAssertions.add(policyAssertion);
    }

    /* (non-Javadoc)
     * @see org.fabric3.scdl.policy.PolicyOperatorParent#getPolicyOperators()
     */
    public Set<PolicyOperator> getPolicyOperators() {
        return policyOperators;
    }

    /* (non-Javadoc)
     * @see org.fabric3.scdl.policy.PolicyOperatorParent#addPolicyOperator(org.fabric3.scdl.policy.PolicyOperator)
     */
    public void addPolicyOperator(PolicyOperator policyOperator) {
        policyOperators.add(policyOperator);
    }

}
