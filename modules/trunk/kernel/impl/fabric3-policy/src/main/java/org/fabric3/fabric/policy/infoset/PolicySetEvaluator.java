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
