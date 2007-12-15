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

import org.fabric3.binding.ws.model.logical.WsBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.spi.generator.BindingGeneratorDelegate;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.idl.java.JavaServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 * 
 * TODO Add support for WSDL Contract
 */
public class Axis2BindingGeneratorDelegate implements BindingGeneratorDelegate<WsBindingDefinition> {
    
    private ClassLoaderGenerator classLoaderGenerator;

    public Axis2BindingGeneratorDelegate(@Reference ClassLoaderGenerator classLoaderGenerator) {
        this.classLoaderGenerator = classLoaderGenerator;
    }

    /**
     * @see org.fabric3.spi.generator.BindingGeneratorDelegate#generateWireSource(org.fabric3.spi.model.instance.LogicalBinding, 
     *                                                                            java.util.Set, 
     *                                                                            org.fabric3.spi.generator.GeneratorContext, 
     *                                                                            org.fabric3.scdl.ServiceDefinition)
     */
    public Axis2WireSourceDefinition generateWireSource(LogicalBinding<WsBindingDefinition> binding,
                                                           Set<Intent> intentsToBeProvided,
                                                           Set<Element> policySetsToBeProvided, 
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
        
        return hwsd;
        
    }

    /**
     * @see org.fabric3.spi.generator.BindingGeneratorDelegate#generateWireTarget(org.fabric3.spi.model.instance.LogicalBinding, 
     *                                                                            java.util.Set, 
     *                                                                            org.fabric3.spi.generator.GeneratorContext, 
     *                                                                            org.fabric3.scdl.ReferenceDefinition)
     */
    public Axis2WireTargetDefinition generateWireTarget(LogicalBinding<WsBindingDefinition> binding,
                                                        Set<Intent> intentsToBeProvided, 
                                                        Set<Element> policySetsToBeProvided,
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
        
        return hwtd;

    }

}
