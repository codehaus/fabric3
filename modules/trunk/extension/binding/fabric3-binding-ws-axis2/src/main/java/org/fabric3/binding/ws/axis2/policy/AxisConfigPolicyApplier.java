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
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.transform.xml.Element2Stream;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Applies policies based on Axis2 configuration parameter.
 * 
 * @version $Revision$ $Date$
 */
public class AxisConfigPolicyApplier implements PolicyApplier {
    
    private final Element2Stream transformer = new Element2Stream(XMLInputFactory.newInstance());

    public void applyPolicy(AxisService axisService, Element policy) {
        
        try {
        
            NodeList parameters = policy.getElementsByTagName("parameter");
            
            for (int i = 0;i < parameters.getLength();i++) {
                
                Element parameterElement = (Element) parameters.item(i);
                String parameterName = parameterElement.getAttribute("name");
                Element actionElement = (Element) parameterElement.getElementsByTagName("action").item(0);
                
                XMLStreamReader reader = transformer.transform(actionElement, null);
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                OMElement policyElement = builder.getDocumentElement();

                axisService.addParameter(parameterName, policyElement);
                
            }

            
        } catch (TransformationException e) {
            throw new AssertionError(e);
        } catch (AxisFault e) {
            throw new AssertionError(e);
        }

    }

}
