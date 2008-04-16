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
package org.fabric3.fabric.command;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class ClassloaderProvisionCommandGenerator implements CommandGenerator {
    private static final ClassloaderProvisionComparator COMPARATOR = new ClassloaderProvisionComparator();
    private final ClassLoaderGenerator classLoaderGenerator;
    private final int order;

    public ClassloaderProvisionCommandGenerator(@Reference ClassLoaderGenerator classLoaderGenerator, @Property(name = "order")int order) {
        this.classLoaderGenerator = classLoaderGenerator;
        this.order = order;
    }

    @SuppressWarnings("unchecked")
    public ClassloaderProvisionCommand generate(LogicalComponent<?> component) throws GenerationException {

        ClassloaderProvisionCommand command = new ClassloaderProvisionCommand(order);

        if (component instanceof LogicalCompositeComponent) {

            LogicalCompositeComponent compositeComponent = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : compositeComponent.getComponents()) {
                command.addPhysicalClassLoaderDefinitions(generate(child).getPhysicalClassLoaderDefinitions());
            }

        } else if (!component.isProvisioned()) {
            PhysicalClassLoaderDefinition physicalClassLoaderDefinition = classLoaderGenerator.generate(component);
            command.addPhysicalClassLoaderDefinition(physicalClassLoaderDefinition);
        }
        // order the creation of classloaders by ensuring parents are listed first. 
        List<PhysicalClassLoaderDefinition> definitions = new ArrayList<PhysicalClassLoaderDefinition>();
        definitions.addAll(command.getPhysicalClassLoaderDefinitions());
        Collections.sort(definitions, COMPARATOR);
        // replace existing order
        command.getPhysicalClassLoaderDefinitions().clear();
        command.getPhysicalClassLoaderDefinitions().addAll(definitions);
        return command;

    }

    private static class ClassloaderProvisionComparator implements Comparator<PhysicalClassLoaderDefinition> {
        public int compare(PhysicalClassLoaderDefinition first, PhysicalClassLoaderDefinition second) {
            return first.getUri().compareTo(second.getUri());
        }
    }


}
