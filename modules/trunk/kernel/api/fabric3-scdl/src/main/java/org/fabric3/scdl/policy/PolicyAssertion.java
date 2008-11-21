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
package org.fabric3.scdl.policy;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;


/**
 * Represents a policy operator.
 *
 */
public class PolicyAssertion extends PolicyNode implements PolicyOperatorParent {
    
    private boolean optional;
    private boolean ignorable;
    private Set<PolicyOperator> policyOperators = new HashSet<PolicyOperator>();
    private Set<AssertionParameter> assertionParameters = new HashSet<AssertionParameter>();
    
    public PolicyAssertion(QName qname) {
        super(qname);
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isIgnorable() {
        return ignorable;
    }

    public void setIgnorable(boolean ignorable) {
        this.ignorable = ignorable;
    }

    public Set<PolicyOperator> getPolicyOperators() {
        return policyOperators;
    }

    public void addPolicyOperator(PolicyOperator policyOperator) {
        policyOperators.add(policyOperator);
    }

    public Set<AssertionParameter> getAssertionParameters() {
        return assertionParameters;
    }

    public void addAssertionParameter(AssertionParameter assertionParameter) {
        assertionParameters.add(assertionParameter);
    }

}
