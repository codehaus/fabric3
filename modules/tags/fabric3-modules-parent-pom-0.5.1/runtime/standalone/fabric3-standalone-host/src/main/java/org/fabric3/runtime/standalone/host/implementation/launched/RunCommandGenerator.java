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
package org.fabric3.runtime.standalone.host.implementation.launched;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.Implementation;
import org.fabric3.spi.generator.AddCommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class RunCommandGenerator implements AddCommandGenerator {
    private GeneratorRegistry registry;
    private int order;

    public RunCommandGenerator(@Reference GeneratorRegistry registry, @Property(name = "order")int order) {
        this.registry = registry;
        this.order = order;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public int getOrder() {
        return order;
    }

    public RunCommand generate(LogicalComponent<?> component) throws GenerationException {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        if (!Launched.class.isInstance(implementation)) {
            return null;
        }
        return new RunCommand(0, component.getUri());
    }

}

