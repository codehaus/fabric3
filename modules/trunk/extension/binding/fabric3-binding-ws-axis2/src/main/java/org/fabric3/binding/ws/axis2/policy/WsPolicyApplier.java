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
package org.fabric3.binding.ws.axis2.policy;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.description.AxisService;
import org.apache.neethi.PolicyEngine;
import org.fabric3.transform.xml.Element2Stream;
import org.w3c.dom.Element;

/**
 * Applies policies based on WS-Policy.
 * 
 * @version $Revision$ $Date$
 */
public class WsPolicyApplier implements PolicyApplier {
    
    private final Element2Stream transformer = new Element2Stream(XMLInputFactory.newInstance());

    public void applyPolicy(AxisService axisService, Element policy) {
        
        try {
            
            XMLStreamReader reader = transformer.transform(policy, null);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement policyElement = builder.getDocumentElement();
            
            axisService.applyPolicy(PolicyEngine.getPolicy(policyElement));
            
        } catch(Exception e) {
            // TODO Handle execption properly
            throw new AssertionError(e);
        }

    }

}
