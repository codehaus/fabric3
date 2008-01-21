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
package org.fabric3.binding.ws.cxf.physical;

import java.net.URI;

import org.fabric3.binding.ws.model.logical.WsBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGeneratorDelegate;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.idl.java.JavaServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * Implementation of the hessian binding generator.
 *
 * @version $Revision: 1560 $ $Date: 2007-10-20 10:02:18 +0100 (Sat, 20 Oct 2007) $
 */
@EagerInit
public class CxfBindingGeneratorDelegate implements BindingGeneratorDelegate<WsBindingDefinition> {
    private ClassLoaderGenerator classLoaderGenerator;

    public CxfBindingGeneratorDelegate(@Reference ClassLoaderGenerator classLoaderGenerator) {
        this.classLoaderGenerator = classLoaderGenerator;
    }

    public CxfWireSourceDefinition generateWireSource(LogicalBinding<WsBindingDefinition> logicalBinding,
                                                     Policy policy,
                                                     GeneratorContext generatorContext,
                                                     ServiceDefinition serviceDefinition) throws GenerationException {
        CxfWireSourceDefinition hwsd = new CxfWireSourceDefinition();
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

    public CxfWireTargetDefinition generateWireTarget(LogicalBinding<WsBindingDefinition> logicalBinding,
                                                     Policy policy,
                                                     GeneratorContext generatorContext,
                                                     ReferenceDefinition referenceDefinition)
            throws GenerationException {

        CxfWireTargetDefinition hwtd = new CxfWireTargetDefinition();
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

}
