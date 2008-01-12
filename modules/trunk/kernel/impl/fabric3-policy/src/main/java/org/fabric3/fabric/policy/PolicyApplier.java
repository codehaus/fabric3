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
package org.fabric3.fabric.policy;

import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * @version $Revision$ $Date$
 */
public interface PolicyApplier {
    
    /**
     * Whether the specified XPath expression applies to the logical binding.
     * 
     * @param logicalBinding Logical binding against which the XPath needs to be applied.
     * @param appliesToXPath XPath expression that needs to be evaluated.
     * @return True if the binding matches the XPath expression.
     */
    boolean doesApply(LogicalBinding<?> logicalBinding, String appliesToXPath);
    
    /**
     * Whether the specified XPath expression applies to the logical component.
     * 
     * @param logicalComponent Logical component against which the XPath needs to be applied.
     * @param appliesToXPath XPath expression that needs to be evaluated.
     * @return True if the component matches the XPath expression.
     */
    boolean doesApply(LogicalComponent<?> logicalComponent, String appliesToXPath);

}
