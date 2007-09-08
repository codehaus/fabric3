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
package org.fabric3.binding.ws.model.physical;

import java.net.URI;
import java.util.Set;

import org.fabric3.binding.ws.model.logical.WsBindingDefinition;
import org.fabric3.extension.generator.BindingGeneratorExtension;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.idl.java.JavaServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * Implementation of the hessian binding generator.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class WsBindingGenerator extends BindingGeneratorExtension<WsWireSourceDefinition, WsWireTargetDefinition, WsBindingDefinition> {
    private ClassLoaderGenerator classLoaderGenerator;

    public WsBindingGenerator(@Reference ClassLoaderGenerator classLoaderGenerator) {
        this.classLoaderGenerator = classLoaderGenerator;
    }

    public WsWireSourceDefinition generateWireSource(LogicalBinding<WsBindingDefinition> logicalBinding,
                                                     Set<Intent> intentsToBeProvided,
                                                     GeneratorContext generatorContext,
                                                     ServiceDefinition serviceDefinition) throws GenerationException {
        WsWireSourceDefinition hwsd = new WsWireSourceDefinition();
        hwsd.setUri(logicalBinding.getBinding().getTargetUri());
        ServiceContract<?> contract = serviceDefinition.getServiceContract();
        if (!(JavaServiceContract.class.isInstance(contract))) {
            throw new UnsupportedOperationException("Temporarily unsupported: interfaces must be Java types");
        }
        hwsd.setServiceInterface((JavaServiceContract.class.cast(contract).getInterfaceClass()));
        URI classloader = classLoaderGenerator.generate(logicalBinding, generatorContext);
        hwsd.setClassloaderURI(classloader);
        return hwsd;

    }

    public WsWireTargetDefinition generateWireTarget(LogicalBinding<WsBindingDefinition> logicalBinding,
                                                     Set<Intent> intentsToBeProvided,
                                                     GeneratorContext generatorContext,
                                                     ReferenceDefinition referenceDefinition)
            throws GenerationException {

        WsWireTargetDefinition hwtd = new WsWireTargetDefinition();
        hwtd.setUri(logicalBinding.getBinding().getTargetUri());
        ServiceContract<?> contract = referenceDefinition.getServiceContract();
        if (!(JavaServiceContract.class.isInstance(contract))) {
            throw new UnsupportedOperationException("Temporarily unsupported: interfaces must be Java types");
        }
        hwtd.setReferenceInterface((JavaServiceContract.class.cast(contract).getInterfaceClass()));
        URI classloader = classLoaderGenerator.generate(logicalBinding, generatorContext);
        hwtd.setClassloaderURI(classloader);
        return hwtd;

    }

    /**
     * @see org.fabric3.extension.generator.BindingGeneratorExtension#getBindingDefinitionClass()
     */
    @Override
    protected Class<WsBindingDefinition> getBindingDefinitionClass() {
        return WsBindingDefinition.class;
    }

}
