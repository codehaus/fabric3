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
package org.fabric3.policy.infoset;

import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public interface PolicySetEvaluator {
    
    /**
     * Whether the policy set applies for the target element. The target element 
     * represents the infoset for either the parent of a binding or implementation.
     * 
     * @param target Target element against which the XPath is evaluated.
     * @param appliesToXPath XPath expression specified against the policy set.
     * @param operation Operation against which the intents are evaluated.
     * @return True if the policy set applies against the target element.
     */
    boolean doesApply(Element target, String appliesToXPath, String operation);

}
