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
package org.fabric3.binding.ws.axis2.physical;

import java.util.Set;

import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public interface Axis2PolicyAware {

    /**
     * @return Policy definitions.
     */
    public Set<Element> getPolicyDefinitions();

    /**
     * @param policyDefinitions Policy definitions.
     */
    public void setPolicyDefinitions(Set<Element> policyDefinitions);

    /**
     * @param policyDefinition Policy definition.
     */
    public void addPolicyDefinition(Element policyDefinition);

}
