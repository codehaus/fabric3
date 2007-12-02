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
package org.fabric3.fabric.async;

import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.Constants;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;

/**
 * Creates {@link NonBlockingInterceptorDefinition}s for one-way operations.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class NonBlockingGenerator implements InterceptorDefinitionGenerator {
    
    private static final QName QNAME = new QName(Constants.FABRIC3_NS, "oneWayPolicy");
    
    private GeneratorRegistry registry;

    public NonBlockingGenerator(@Reference GeneratorRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(QNAME, this);
    }

    public PhysicalInterceptorDefinition generate(PolicySet policySet, GeneratorContext generatorContext) {
        return new NonBlockingInterceptorDefinition();
    }
}
