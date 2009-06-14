/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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
