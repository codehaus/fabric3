/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.fabric.generator.component;

import java.net.URI;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.BuildComponentCommand;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorNotFoundException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * Creates a command to build a component on a runtime.
 *
 * @version $Revision$ $Date$
 */
public class BuildComponentCommandGenerator implements CommandGenerator {

    private final GeneratorRegistry generatorRegistry;
    private final int order;

    public BuildComponentCommandGenerator(@Reference GeneratorRegistry generatorRegistry, @Property(name = "order") int order) {
        this.generatorRegistry = generatorRegistry;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    @SuppressWarnings("unchecked")
    public BuildComponentCommand generate(LogicalComponent<?> component) throws GenerationException {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        if (!(component instanceof LogicalCompositeComponent) && component.getState() == LogicalState.NEW) {
            Class<? extends Implementation> type = implementation.getClass();
            ComponentGenerator generator = generatorRegistry.getComponentGenerator(type);
            if (generator == null) {
                throw new GeneratorNotFoundException(type);
            }
            PhysicalComponentDefinition definition = generator.generate(component);
            URI uri = component.getUri();
            definition.setComponentId(uri);
            definition.setClassLoaderId(component.getDefinition().getContributionUri());
            QName deployable = component.getDeployable();
            definition.setDeployable(deployable);
            return new BuildComponentCommand(order, definition);
        }
        return null;
    }

}
