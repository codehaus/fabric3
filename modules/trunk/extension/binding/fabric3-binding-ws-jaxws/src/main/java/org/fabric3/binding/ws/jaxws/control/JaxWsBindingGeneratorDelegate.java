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

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
