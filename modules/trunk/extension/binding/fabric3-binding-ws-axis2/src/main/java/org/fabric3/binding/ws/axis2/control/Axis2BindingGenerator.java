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
 *
 */
package org.fabric3.binding.ws.axis2.control;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.fabric3.binding.ws.axis2.provision.Axis2PolicyAware;
import org.fabric3.binding.ws.axis2.provision.Axis2WireSourceDefinition;
import org.fabric3.binding.ws.axis2.provision.Axis2WireTargetDefinition;
import org.fabric3.binding.ws.axis2.provision.AxisPolicy;
import org.fabric3.binding.ws.provision.WsdlElement;
import org.fabric3.binding.ws.scdl.WsBindingDefinition;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.Namespaces;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @version $Revision$ $Date$
 * 
 * TODO Add support for WSDL Contract
 */
public class Axis2BindingGenerator  implements BindingGenerator<Axis2WireSourceDefinition, Axis2WireTargetDefinition, WsBindingDefinition> {
    
    private static final QName POLICY_ELEMENT = new QName(Namespaces.POLICY, "axisPolicy");
    @Reference protected ClassLoaderRegistry classLoaderRegistry;

    public Axis2WireSourceDefinition generateWireSource(LogicalBinding<WsBindingDefinition> binding,
                                                        Policy policy,  
                                                        ServiceDefinition serviceDefinition) throws GenerationException {
        
        Axis2WireSourceDefinition hwsd = new Axis2WireSourceDefinition();
        hwsd.setUri(binding.getDefinition().getTargetUri());
        
        ServiceContract<?> contract = serviceDefinition.getServiceContract();
        hwsd.setServiceInterface(contract.getQualifiedInterfaceName());
        
        setPolicyConfigs(hwsd, policy, contract);
        
        return hwsd;
        
    }

    public Axis2WireTargetDefinition generateWireTarget(LogicalBinding<WsBindingDefinition> binding,
                                                        Policy policy,
                                                        ReferenceDefinition referenceDefinition) throws GenerationException {

        Axis2WireTargetDefinition hwtd = new Axis2WireTargetDefinition();        
        WsdlElement wsdlElement = parseWsdlElement(binding.getDefinition().getWsdlElement());
        hwtd.setWsdlElement(wsdlElement);
        hwtd.setWsdlLocation(binding.getDefinition().getWsdlLocation());
        
        hwtd.setUri(binding.getDefinition().getTargetUri());
        
        ServiceContract<?> contract = referenceDefinition.getServiceContract();
        hwtd.setReferenceInterface(contract.getQualifiedInterfaceName());
        
        //Set Axis2 operation parameters
        addOperationInfo(hwtd, contract);
        
        //Set config
        hwtd.setConfig(binding.getDefinition().getConfig());
        
        setPolicyConfigs(hwtd, policy, contract);
        
        return hwtd;

    }
    
    private void addOperationInfo(Axis2WireTargetDefinition hwtd, ServiceContract<?> serviceContract) {
        for (Operation<?> operation : serviceContract.getOperations()) {
            Map<String, String> info = operation.getInfo(org.fabric3.binding.ws.axis2.common.Constant.AXIS2_JAXWS_QNAME);
            if(info != null) {
                hwtd.addOperationInfo(operation.getName(), info);
            }	    
        }	
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
    
    private WsdlElement parseWsdlElement(String wsdlElement) throws GenerationException {
        if(wsdlElement == null) {
            return null;
        }
        
        String[] token = wsdlElement.split("#");
        String namespaceUri = token[0];
        
        if (!token[1].startsWith("wsdl.port")) {
            throw new GenerationException("Only WSDL 1.1 ports are currently supported");
        }
        token = token[1].substring(token[1].indexOf('(') + 1, token[1].indexOf(')')).split("/");

        QName serviceName = new QName(namespaceUri, token[0]);
        QName portName = new QName(namespaceUri, token[1]);
        
        return new WsdlElement(serviceName, portName);        
    }
    
}
