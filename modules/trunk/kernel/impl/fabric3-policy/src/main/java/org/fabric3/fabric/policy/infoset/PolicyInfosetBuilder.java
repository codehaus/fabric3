/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.fabric.policy.infoset;

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
