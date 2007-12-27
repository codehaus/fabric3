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
package org.fabric3.fabric.implementation.singleton;

import java.net.URI;
import java.util.Set;

import org.fabric3.scdl.definitions.Intent;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * @version $Rev$ $Date$
 */
public class SingletonGenerator implements ComponentGenerator<LogicalComponent<SingletonImplementation>> {

    public SingletonGenerator(@Reference GeneratorRegistry registry) {
        registry.register(SingletonImplementation.class, this);
    }

    public PhysicalComponentDefinition generate(LogicalComponent<SingletonImplementation> component, 
                                                GeneratorContext context) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<SingletonImplementation> source,
                                                           LogicalReference reference,
                                                           boolean optimizable,  
                                                           Set<Intent> intentsToBeProvided,
                                                           Set<Element> policySetsToBeProvided,
                                                           GeneratorContext context) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service, 
                                                           LogicalComponent<SingletonImplementation> logical,  
                                                           Set<Intent> intentsToBeProvided,
                                                           Set<Element> policySetsToBeProvided,
                                                           GeneratorContext context) throws GenerationException {
        SingletonWireTargetDefinition wireDefinition = new SingletonWireTargetDefinition();
        URI uri = logical.getUri().resolve(service.getUri());
        wireDefinition.setUri(uri);
        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<SingletonImplementation> source, 
                                                                   LogicalResource<?> resource,
                                                                   GeneratorContext context) throws GenerationException {
        throw new UnsupportedOperationException();
    }
    
}
