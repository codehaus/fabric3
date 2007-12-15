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
package org.fabric3.spi.policy.registry;

import javax.xml.namespace.QName;

import org.fabric3.scdl.definitions.PolicyPhase;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class PolicyResult {
    
    private final Element policyDefinition;
    private final PolicyPhase policyPhase;
    
    /**
     * Initiaizes the policy definition and phase at which policy is applied.
     * 
     * @param policyDefinition Opaque policy definition.
     * @param policyPhase Policy phase.
     */
    public PolicyResult(Element policyDefinition, PolicyPhase policyPhase) {
        this.policyDefinition = policyDefinition;
        this.policyPhase = policyPhase;
    }

    /**
     * @return Opaque policy definition.
     */
    public Element getPolicyDefinition() {
        return policyDefinition;
    }

    /**
     * @return Phase at which policy is applied.
     */
    public PolicyPhase getPolicyPhase() {
        return policyPhase;
    }
    
    /**
     * @return Qualified name of the extension element.
     */
    public QName getQualifiedName() {
        return new QName(policyDefinition.getNamespaceURI(), policyDefinition.getLocalName());
    }

}
