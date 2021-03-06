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
package org.fabric3.policy.infoset;

import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.w3c.dom.Element;

/**
 * Builds an infoset against which the policy set is evaluated.
 * 
 * @version $Revision$ $Date$
 */
public interface PolicyInfosetBuilder {
    
    /**
     * Builds the infoset to evaluate the policy expression for an interaction intent.
     * 
     * @param logicalBinding Target binding.
     * @return Infoset against whch whether the policy appplies is checked.
     */
    Element buildInfoSet(LogicalBinding<?> logicalBinding);
    
    /**
     * Builds the infoset to evaluate the policy expression for an implementation intent.
     * 
     * @param logicalComponent Target component.
     * @return Infoset against whch whether the policy appplies is checked.
     */
    Element buildInfoSet(LogicalComponent<?> logicalComponent);

}
