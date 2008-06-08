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
package org.fabric3.binding.ftp.control;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.fabric3.binding.ftp.common.Constants;
import org.fabric3.binding.ftp.provision.FtpSecurity;
import org.fabric3.binding.ftp.provision.FtpWireSourceDefinition;
import org.fabric3.binding.ftp.provision.FtpWireTargetDefinition;
import org.fabric3.binding.ftp.scdl.FtpBindingDefinition;
import org.fabric3.binding.ftp.scdl.TransferMode;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;
import org.w3c.dom.Element;

/**
 *
 * @version $Revision$ $Date$
 * @param <HessianBindingDefinition>
 * @param <HessianWireSourceDefinition>
 */
public class FtpBindingGenerator implements BindingGenerator<FtpWireSourceDefinition, FtpWireTargetDefinition, FtpBindingDefinition> {

    public FtpWireSourceDefinition generateWireSource(LogicalBinding<FtpBindingDefinition> binding, 
                                                      Policy policy, 
                                                      ServiceDefinition serviceDefinition) throws GenerationException {
        
        ServiceContract<?> serviceContract = serviceDefinition.getServiceContract();
        if (serviceContract.getOperations().size() != 1) {
            throw new GenerationException("Expects only one operation");
        }


        URI id = binding.getParent().getParent().getParent().getUri();
        FtpWireSourceDefinition hwsd = new FtpWireSourceDefinition(id);
        hwsd.setUri(binding.getBinding().getTargetUri());
        
        return hwsd;
        
    }

    public FtpWireTargetDefinition generateWireTarget(LogicalBinding<FtpBindingDefinition> binding, 
                                                      Policy policy,
                                                      ReferenceDefinition referenceDefinition) throws GenerationException {
        
        ServiceContract<?> serviceContract = referenceDefinition.getServiceContract();
        if (serviceContract.getOperations().size() != 1) {
            throw new GenerationException("Expects only one operation");
        }

        URI id = binding.getParent().getParent().getParent().getUri();
        boolean active = binding.getBinding().getTransferMode() == TransferMode.ACTIVE;
        
        FtpSecurity security = processPolicies(policy, serviceContract.getOperations().iterator().next());

        FtpWireTargetDefinition hwtd = new FtpWireTargetDefinition(id, active, security);
        hwtd.setUri(binding.getBinding().getTargetUri());
        
        return hwtd;
        
    }

    private FtpSecurity processPolicies(Policy policy, Operation<?> operation) throws GenerationException {
        
        List<PolicySet> policySets = policy.getProvidedPolicySets(operation);
        if (policySets.size() == 0) {
            return null;
        }
        if (policySets.size() != 1) {
            throw new GenerationException("Invalid policy configuration, only supports security policy");
        }
        
        PolicySet policySet = policySets.iterator().next();
        
        QName policyQName = policySet.getExtensionName();
        if (!policyQName.equals(Constants.POLICY_QNAME)) {
            throw new GenerationException("Unexpected policy element " + policyQName);
        }
        
        Element policyElement = policySet.getExtension();
        String user = policyElement.getAttribute("user");
        if (user == null) {
            throw new GenerationException("User name not specified in security policy");
        }
        String password = policyElement.getAttribute("password");
        if (password == null) {
            throw new GenerationException("Password not specified in security policy");
        }
            
        return new FtpSecurity(user, password);
        
    }
    
}
