/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 */
package org.fabric3.loader.definitions;

import java.io.InputStream;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.model.type.policy.AllPolicyOperator;
import org.fabric3.model.type.policy.ComplexAssertionParameter;
import org.fabric3.model.type.policy.ExactlyOnePolicyOperator;
import org.fabric3.model.type.policy.PolicyAssertion;
import org.fabric3.model.type.policy.PolicyNode;
import org.fabric3.model.type.policy.PolicyOperator;
import org.fabric3.model.type.policy.PolicyOperatorParent;
import org.fabric3.model.type.policy.SimpleAssertionParameter;

public class WsPolicyLoader {
    
    private static final QName POLICY =  new QName("http://schemas.xmlsoap.org/ws/2004/09/policy", "Policy");
    private static final QName ALL =  new QName("http://schemas.xmlsoap.org/ws/2004/09/policy", "All");
    private static final QName EXACTLY_ONE =  new QName("http://schemas.xmlsoap.org/ws/2004/09/policy", "ExactlyOne");
    

    /**
     * @param args
     * @throws FactoryConfigurationError 
     * @throws XMLStreamException 
     */
    public static void main(String[] args) throws XMLStreamException, FactoryConfigurationError {
        
        InputStream stream = WsPolicyLoader.class.getResourceAsStream("policy.xml");
        
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        
        Stack<PolicyNode> policyNodes = new Stack<PolicyNode>();
        
        PolicyNode root = null;
        
        while (reader.hasNext()) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                QName name = reader.getName();
                System.err.println(name);
                if (POLICY.equals(name) || ALL.equals(name)) {
                    AllPolicyOperator allPolicyOperator = new AllPolicyOperator(ALL);
                    if (!policyNodes.isEmpty()) {
                        PolicyOperatorParent parent = (PolicyOperatorParent) policyNodes.peek();
                        parent.addPolicyOperator(allPolicyOperator);
                    }
                    policyNodes.push(allPolicyOperator);
                } else if (EXACTLY_ONE.equals(name)) {
                    ExactlyOnePolicyOperator exactlyOnePolicyOperator = new ExactlyOnePolicyOperator(name);
                    if (!policyNodes.isEmpty()) {
                        PolicyOperatorParent parent = (PolicyOperatorParent) policyNodes.peek();
                        parent.addPolicyOperator(exactlyOnePolicyOperator);
                    }
                    policyNodes.push(exactlyOnePolicyOperator);
                } else {
                    PolicyNode node = policyNodes.peek();
                    if (node instanceof PolicyOperator) {
                        PolicyOperator parent = (PolicyOperator) node;
                        PolicyAssertion child = new PolicyAssertion(name);
                        parent.addPolicyAssertion(child);
                        policyNodes.push(child);
                    } else if (node instanceof PolicyAssertion) {
                        PolicyAssertion parent = (PolicyAssertion) node;
                        ComplexAssertionParameter child = new ComplexAssertionParameter(name);
                        parent.addAssertionParameter(child);
                        policyNodes.push(child);
                        for (int i = 0; i< reader.getAttributeCount();i++) {
                            SimpleAssertionParameter simpleAssertionParameter = new SimpleAssertionParameter(reader.getAttributeName(i));
                            simpleAssertionParameter.setValue(reader.getAttributeValue(i));
                            child.addSimpleAssertionParameter(simpleAssertionParameter);
                        }
                    } else if (node instanceof ComplexAssertionParameter) {
                        ComplexAssertionParameter parent = (ComplexAssertionParameter) node;
                        ComplexAssertionParameter child = new ComplexAssertionParameter(name);
                        parent.addComplexAssertionParameter(child);
                        policyNodes.push(child);
                        for (int i = 0; i< reader.getAttributeCount();i++) {
                            SimpleAssertionParameter simpleAssertionParameter = new SimpleAssertionParameter(reader.getAttributeName(i));
                            simpleAssertionParameter.setValue(reader.getAttributeValue(i));
                            child.addSimpleAssertionParameter(simpleAssertionParameter);
                        }
                    }
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                root = policyNodes.pop();
                break;
            }
        }
        
        System.err.println(root);

    }

}
