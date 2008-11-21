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

import javax.xml.namespace.QName;


/**
 * Represents a policy operator.
 *
 */
public class PolicyAssertion extends PolicyNode {

    private PolicyOperator policyOperator;
    private boolean optional;
    private boolean ignorable;
    
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

    public PolicyOperator getPolicyOperator() {
        return policyOperator;
    }

    public void setPolicyOperator(PolicyOperator policyOperator) {
        this.policyOperator = policyOperator;
    }

}
