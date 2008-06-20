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
package org.fabric3.binding.ws.axis2.control;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;

import org.fabric3.binding.ws.axis2.provision.Axis2PolicyAware;
import org.fabric3.binding.ws.axis2.provision.Axis2WireSourceDefinition;
import org.fabric3.binding.ws.axis2.provision.Axis2WireTargetDefinition;
import org.fabric3.binding.ws.axis2.provision.AxisPolicy;
import org.fabric3.binding.ws.scdl.WsBindingDefinition;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.Constants;
import org.fabric3.spi.generator.BindingGeneratorDelegate;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @version $Revision$ $Date$
 * 
 * TODO Add support for WSDL Contract
 */
public class Axis2BindingGeneratorDelegate implements BindingGeneratorDelegate<WsBindingDefinition> {
    
    private static final QName POLICY_ELEMENT = new QName(Constants.FABRIC3_NS, "axisPolicy");

    public Axis2WireSourceDefinition generateWireSource(LogicalBinding<WsBindingDefinition> binding,
                                                        Policy policy,  
                                                        ServiceDefinition serviceDefinition) throws GenerationException {
        
        Axis2WireSourceDefinition hwsd = new Axis2WireSourceDefinition();
        hwsd.setUri(binding.getBinding().getTargetUri());
        
        ServiceContract<?> contract = serviceDefinition.getServiceContract();
        hwsd.setServiceInterface(contract.getQualifiedInterfaceName());
        
        URI classloaderId = binding.getParent().getParent().getClassLoaderId();
        hwsd.setClassloaderURI(classloaderId);
        
        setPolicyConfigs(hwsd, policy, contract);
        
        return hwsd;
        
    }

    public Axis2WireTargetDefinition generateWireTarget(LogicalBinding<WsBindingDefinition> binding,
                                                        Policy policy,
                                                        ReferenceDefinition referenceDefinition) throws GenerationException {

        Axis2WireTargetDefinition hwtd = new Axis2WireTargetDefinition();
        hwtd.setUri(binding.getBinding().getTargetUri());
        
        ServiceContract<?> contract = referenceDefinition.getServiceContract();
        hwtd.setReferenceInterface(contract.getQualifiedInterfaceName());
        
        URI classloaderId = binding.getParent().getParent().getClassLoaderId();
        hwtd.setClassloaderURI(classloaderId);
        
        setPolicyConfigs(hwtd, policy, contract);
        
        return hwtd;

    }
    
    private void setPolicyConfigs(Axis2PolicyAware policyAware, Policy policy, ServiceContract<?> serviceContract) throws Axis2GenerationException {
        
        for (Operation<?> operation : serviceContract.getOperations()) {
            
            List<PolicySet> policySets = policy.getProvidedPolicySets(operation);
            if (policySets == null) {
                continue;
            }
            
            for (PolicySet policySet : policy.getProvidedPolicySets(operation)) {
                
                Element policyDefinition = policySet.getExtension();
                QName qname = new QName(policyDefinition.getNamespaceURI(), policyDefinition.getNodeName());
                if (POLICY_ELEMENT.equals(qname)) {
                    throw new Axis2GenerationException("Unknow policy element " + qname);
                }
                
                String module = policyDefinition.getAttribute("module");
                String message = policyDefinition.getAttribute("message");
                Element opaquePolicy = null;
                
                NodeList nodeList = policyDefinition.getChildNodes();
                for (int i = 0;i < nodeList.getLength();i++) {
                    if (nodeList.item(i) instanceof Element) {
                        opaquePolicy = (Element) nodeList.item(i);
                        break;
                    }
                }
                
                AxisPolicy axisPolicy = new AxisPolicy(message, module, opaquePolicy);
                policyAware.addPolicy(operation.getName(), axisPolicy);
                
            }
            
        }
        
    }

}
