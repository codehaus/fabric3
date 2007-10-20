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
package org.fabric3.extension.generator;

import org.fabric3.scdl.definitions.PolicySetExtension;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Abstract super class for all the interceptor definition generators.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public abstract class InterceptorDefinitionGeneratorExtension<PSE extends PolicySetExtension, PID extends PhysicalInterceptorDefinition>
        implements InterceptorDefinitionGenerator<PSE, PID> {
    
    // Generator registry
    private GeneratorRegistry generatorRegistry;
    
    /**
     * Injects the generator registry.
     * 
     * @param generatorRegistry Injected generator registry.
     */
    @Reference
    public void setGeneratorRegistry(GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }
    
    /**
     * Registers with the registry.
     */
    @Init
    public void start() {
        generatorRegistry.register(getPolicyExtensionClass(), this);
    }
    
    /**
     * Returns the binding definition class.
     * 
     * @return Binding definition class.
     */
    protected abstract Class<PSE> getPolicyExtensionClass();

}
