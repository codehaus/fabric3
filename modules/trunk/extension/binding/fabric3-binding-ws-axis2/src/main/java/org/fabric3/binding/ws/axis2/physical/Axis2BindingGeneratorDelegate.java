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
package org.fabric3.binding.ws.axis2.physical;

import java.net.URI;
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.binding.ws.model.logical.WsBindingDefinition;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.generator.BindingGeneratorDelegate;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.idl.java.JavaServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.PolicyResult;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 * 
 * TODO Add support for WSDL Contract
 */
public class Axis2BindingGeneratorDelegate implements BindingGeneratorDelegate<WsBindingDefinition> {
    
    private ClassLoaderGenerator classLoaderGenerator;
    private static final QName POLICY_ELEMENT_NAME = new QName("", "policyConfig");

    public Axis2BindingGeneratorDelegate(@Reference ClassLoaderGenerator classLoaderGenerator) {
        this.classLoaderGenerator = classLoaderGenerator;
    }

    public Axis2WireSourceDefinition generateWireSource(LogicalBinding<WsBindingDefinition> binding,
                                                        PolicyResult policyResult, 
                                                        GeneratorContext context, 
                                                        ServiceDefinition serviceDefinition) throws GenerationException {
        
        Axis2WireSourceDefinition hwsd = new Axis2WireSourceDefinition();
        hwsd.setUri(binding.getBinding().getTargetUri());
        
        ServiceContract<?> contract = serviceDefinition.getServiceContract();
        if (!(JavaServiceContract.class.isInstance(contract))) {
            throw new UnsupportedOperationException("Temporarily unsupported: interfaces must be Java types");
        }
        hwsd.setServiceInterface((JavaServiceContract.class.cast(contract).getInterfaceClass()));
        
        URI classloader = classLoaderGenerator.generate(binding, context);
        hwsd.setClassloaderURI(classloader);
        
        setPolicyConfigs(hwsd, policyResult, contract, true);
        
        return hwsd;
        
    }

    public Axis2WireTargetDefinition generateWireTarget(LogicalBinding<WsBindingDefinition> binding,
                                                        PolicyResult policyResult,
                                                        GeneratorContext context, 
                                                        ReferenceDefinition referenceDefinition) throws GenerationException {

        Axis2WireTargetDefinition hwtd = new Axis2WireTargetDefinition();
        hwtd.setUri(binding.getBinding().getTargetUri());
        
        ServiceContract<?> contract = referenceDefinition.getServiceContract();
        if (!(JavaServiceContract.class.isInstance(contract))) {
            throw new UnsupportedOperationException("Temporarily unsupported: interfaces must be Java types");
        }
        hwtd.setReferenceInterface((JavaServiceContract.class.cast(contract).getInterfaceClass()));
        
        URI classloader = classLoaderGenerator.generate(binding, context);
        hwtd.setClassloaderURI(classloader);
        
        setPolicyConfigs(hwtd, policyResult, contract, false);
        
        return hwtd;

    }
    
    private void setPolicyConfigs(Axis2PolicyAware policyAware, 
                                  PolicyResult policyResult, 
                                  ServiceContract<?> serviceContract,
                                  boolean source) throws Axis2GenerationException {
        
        for (Operation<?> operation : serviceContract.getOperations()) {
            
            Set<PolicySet> policySets = source ? policyResult.getSourcePolicySets(operation) : policyResult.getTargetPolicySets(operation);
            
            for (PolicySet policySet : policySets) {
                
                if (POLICY_ELEMENT_NAME.equals(policySet.getExtensionName())) {
                    throw new Axis2GenerationException("Unsupported policy element:" + policySet.getExtensionName());
                }
                policyAware.addPolicyDefinition(operation.getName(), policySet.getExtension());
            }
            
        }
        
    }

}
