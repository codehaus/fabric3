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
package org.fabric3.binding.ws.jaxws.control;

import java.net.URI;

import org.fabric3.binding.ws.jaxws.provision.JaxWsWireSourceDefinition;
import org.fabric3.binding.ws.jaxws.provision.JaxWsWireTargetDefinition;
import org.fabric3.binding.ws.scdl.WsBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGeneratorDelegate;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;
import org.osoa.sca.annotations.EagerInit;

@EagerInit
public class JaxWsBindingGeneratorDelegate implements BindingGeneratorDelegate<WsBindingDefinition> {

   
    public JaxWsWireSourceDefinition generateWireSource(
            LogicalBinding<WsBindingDefinition> logicalBinding,
            Policy policy,
            ServiceDefinition serviceDefinition) throws GenerationException {
        
        WsBindingDefinition wsdef = logicalBinding.getBinding();
        JaxWsWireSourceDefinition hwsd = new JaxWsWireSourceDefinition();
        hwsd.setUri(wsdef.getTargetUri());
        ServiceContract<?> contract = serviceDefinition.getServiceContract();
        hwsd.setServiceInterface(contract.getQualifiedInterfaceName());
        String wsdlElement = wsdef.getWsdlElement();
        if (wsdlElement == null) {
            hwsd.setPortName(serviceDefinition.getName());
            URI uri = logicalBinding.getParent().getParent().getUri();
            if (uri.getFragment() != null) {
                hwsd.setServiceName(uri.getFragment());
            }
            hwsd.setNamespaceURI(wsdef.getTargetUri() + "");            
        } else {
            hwsd.setWsdlElement(wsdef.getWsdlElement());
        }
        //URI classloaderId = logicalBinding.getParent().getParent().getParent().getUri();
        //hwsd.setClassloaderURI(classloaderId);
        return hwsd;

    }

    public JaxWsWireTargetDefinition generateWireTarget(
            LogicalBinding<WsBindingDefinition> logicalBinding,
            Policy policy,
            ReferenceDefinition referenceDefinition) throws GenerationException {
        JaxWsWireTargetDefinition hwtd = new JaxWsWireTargetDefinition();
        WsBindingDefinition wsdef = logicalBinding.getBinding();
        hwtd.setUri(wsdef.getTargetUri());
        ServiceContract<?> contract = referenceDefinition.getServiceContract();
        hwtd.setReferenceInterface(contract.getQualifiedInterfaceName());
        String wsdlElement = wsdef.getWsdlElement();
        if (wsdlElement == null) {
            hwtd.setPortName(referenceDefinition.getName());
            URI uri = logicalBinding.getParent().getParent().getUri();
            if (uri.getFragment() != null) {
                hwtd.setServiceName(uri.getFragment());
            }
            hwtd.setNamespaceURI(wsdef.getTargetUri() + "");
        } else {
            hwtd.setWsdlElement(logicalBinding.getBinding().getWsdlElement());
        }

        //URI classloaderId = logicalBinding.getParent().getParent().getClassLoaderId();
        //hwtd.setClassloaderURI(classloaderId);
        return hwtd;
    }
}
