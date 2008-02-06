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
package org.fabric3.tx.interceptor;

import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

import org.fabric3.spi.Constants;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.scdl.Operation;

/**
 * Interceptor definition generator for suspend transaction policy extensions.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class TxInterceptorDefinitionGenerator implements InterceptorDefinitionGenerator {
    private static final QName EXTENSION_NAME = new QName(Constants.FABRIC3_NS, "transaction");
    private GeneratorRegistry generatorRegistry;

    public TxInterceptorDefinitionGenerator(@Reference GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    /**
     * Registers with the registry.
     */
    @Init
    public void start() {
        generatorRegistry.register(EXTENSION_NAME, this);
    }

    public PhysicalInterceptorDefinition generate(Element policyDefinition,
                                                  GeneratorContext generatorContext,
                                                  Operation<?> operation,
                                                  LogicalBinding<?> logicalBinding) throws GenerationException {
        String action = policyDefinition.getAttribute("action");

        return new TxInterceptorDefinition(TxAction.valueOf(action));
    }
}
