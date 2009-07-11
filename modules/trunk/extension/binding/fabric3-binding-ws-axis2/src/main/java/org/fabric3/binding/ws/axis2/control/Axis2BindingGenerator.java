/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.binding.ws.axis2.control;

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.fabric3.binding.ws.axis2.provision.Axis2PolicyAware;
import org.fabric3.binding.ws.axis2.provision.Axis2WireSourceDefinition;
import org.fabric3.binding.ws.axis2.provision.Axis2WireTargetDefinition;
import org.fabric3.binding.ws.axis2.provision.AxisPolicy;
import org.fabric3.binding.ws.provision.WsdlElement;
import org.fabric3.binding.ws.model.WsBindingDefinition;
import org.fabric3.host.Namespaces;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.model.type.service.Operation;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.policy.Policy;

/**
 * @version $Rev$ $Date$
 *          <p/>
 *          TODO Add support for WSDL Contract
 */
public class Axis2BindingGenerator implements BindingGenerator<WsBindingDefinition> {

    private static final QName POLICY_ELEMENT = new QName(Namespaces.POLICY, "axisPolicy");
    @Reference
    protected ClassLoaderRegistry classLoaderRegistry;

    public Axis2WireSourceDefinition generateWireSource(LogicalBinding<WsBindingDefinition> binding,
                                                        ServiceContract<?> contract,
                                                        List<LogicalOperation> operations,
                                                        Policy policy) throws GenerationException {

        Axis2WireSourceDefinition hwsd = new Axis2WireSourceDefinition();
        hwsd.setUri(binding.getDefinition().getTargetUri());

        hwsd.setServiceInterface(contract.getQualifiedInterfaceName());

        setPolicyConfigs(hwsd, policy, operations);

        return hwsd;

    }


    public Axis2WireTargetDefinition generateWireTarget(LogicalBinding<WsBindingDefinition> binding,
                                                        ServiceContract<?> contract,
                                                        List<LogicalOperation> operations,
                                                        Policy policy) throws GenerationException {

        Axis2WireTargetDefinition hwtd = new Axis2WireTargetDefinition();
        WsdlElement wsdlElement = parseWsdlElement(binding.getDefinition().getWsdlElement());
        hwtd.setWsdlElement(wsdlElement);
        hwtd.setWsdlLocation(binding.getDefinition().getWsdlLocation());

        hwtd.setUri(binding.getDefinition().getTargetUri());

        hwtd.setReferenceInterface(contract.getQualifiedInterfaceName());

        //Set Axis2 operation parameters
        addOperationInfo(hwtd, contract);

        //Set config
        hwtd.setConfig(binding.getDefinition().getConfiguration());

        setPolicyConfigs(hwtd, policy, operations);

        return hwtd;

    }

    private void addOperationInfo(Axis2WireTargetDefinition hwtd, ServiceContract<?> serviceContract) {
        for (Operation<?> operation : serviceContract.getOperations()) {
            Map<String, String> info = operation.getInfo(org.fabric3.binding.ws.axis2.common.Constant.AXIS2_JAXWS_QNAME);
            if (info != null) {
                hwtd.addOperationInfo(operation.getName(), info);
            }
        }
    }

    private void setPolicyConfigs(Axis2PolicyAware policyAware, Policy policy, List<LogicalOperation> operations) throws Axis2GenerationException {

        for (LogicalOperation operation : operations) {

            List<PolicySet> policySets = policy.getProvidedPolicySets(operation);
            if (policySets == null) {
                continue;
            }

            for (PolicySet policySet : policy.getProvidedPolicySets(operation)) {

                Element policyDefinition = policySet.getExpression();
                QName qname = new QName(policyDefinition.getNamespaceURI(), policyDefinition.getNodeName());
                if (POLICY_ELEMENT.equals(qname)) {
                    throw new Axis2GenerationException("Unknow policy element " + qname);
                }

                String module = policyDefinition.getAttribute("module");
                String message = policyDefinition.getAttribute("message");
                Element opaquePolicy = null;

                NodeList nodeList = policyDefinition.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    if (nodeList.item(i) instanceof Element) {
                        opaquePolicy = (Element) nodeList.item(i);
                        break;
                    }
                }

                AxisPolicy axisPolicy = new AxisPolicy(message, module, opaquePolicy);
                policyAware.addPolicy(operation.getDefinition().getName(), axisPolicy);

            }

        }
    }

    private WsdlElement parseWsdlElement(String wsdlElement) throws GenerationException {
        if (wsdlElement == null) {
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
