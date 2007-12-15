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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.fabric3.transform.xml.Node2String;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class DefaultAxis2PolicyBuilder implements Axis2PolicyBuilder {
    
    private final Node2String transformer = new Node2String();

    public Policy buildPolicy(Element policyDefinition) {
        
        try {
            
            String policy = transformer.transform(policyDefinition, null);
            InputStream inputStream = new ByteArrayInputStream(policy.getBytes());
            
            return PolicyEngine.getPolicy(inputStream);
            
        } catch(Exception e) {
            // TODO Handle execption properly
            throw new AssertionError(e);
        }
        
    }

}
