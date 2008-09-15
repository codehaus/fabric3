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

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class DefaultPolicySetEvaluator implements PolicySetEvaluator {
    
    private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

    public boolean doesApply(Element target, String appliesToXPath, final String operation) {
        
        try {
            
            XPath xpath = XPATH_FACTORY.newXPath();
            
            xpath.setXPathVariableResolver(new XPathVariableResolver() {
                public Object resolveVariable(QName variable) {
                    if ("Operation".equals(variable.getLocalPart())) {
                        return operation;
                    } else {
                        throw new AssertionError("Unexpected variable name " + variable);
                    }
                }
            });
            
            Boolean ret = (Boolean) xpath.evaluate(appliesToXPath, target, XPathConstants.BOOLEAN);
            
            return ret.booleanValue();
            
        } catch (XPathExpressionException ex) {
            throw new AssertionError(ex);
        }
        
    }

}
