/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
package org.fabric3.fabric.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.generator.classloader.ClassLoaderCommandGenerator;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Default implementation of the physical model generator. This implementation topologically sorts components according to their position in the
 * domain hierarchy. That is, by URI. This guarantees commands will be generated in in the proper order. As part of the topological sort, an ordered
 * set of all logical components is created. The set is then iterated and command generators called based on their command order are dispatched to for
 * each logical component.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class PhysicalModelGeneratorImpl implements PhysicalModelGenerator {
    private static final Comparator<LogicalComponent<?>> COMPARATOR = new Comparator<LogicalComponent<?>>() {
        public int compare(LogicalComponent<?> first, LogicalComponent<?> second) {
            return first.getUri().compareTo(second.getUri());
        }
    };

    private final List<CommandGenerator> commandGenerators;
    private ClassLoaderCommandGenerator classLoaderCommandGenerator;

    public PhysicalModelGeneratorImpl(@Reference List<CommandGenerator> commandGenerators,
                                      @Reference ClassLoaderCommandGenerator classLoaderCommandGenerator) {
        this.classLoaderCommandGenerator = classLoaderCommandGenerator;
        // sort the command generators
        this.commandGenerators = sort(commandGenerators);
    }

    public CommandMap generate(Collection<LogicalComponent<?>> components) throws GenerationException {
        List<LogicalComponent<?>> sorted = topologicalSort(components);
        // provision required classloaders first
        CommandMap commandMap = new CommandMap();
        Map<String, Set<Command>> commandsPerZone = classLoaderCommandGenerator.generate(sorted);
        for (Map.Entry<String, Set<Command>> entry : commandsPerZone.entrySet()) {
            for (Command command : entry.getValue()) {
                commandMap.addCommand(entry.getKey(), command);
            }
        }

        // provision components
        for (CommandGenerator generator : commandGenerators) {
            for (LogicalComponent<?> component : sorted) {
                Command command = generator.generate(component);
                if (command != null) {
                    commandMap.addCommand(component.getZone(), command);
                }
            }
        }

        // release classloaders for components being undeployed that are no longer referenced
        Map<String, Set<Command>> releaseCommandsPerZone = classLoaderCommandGenerator.release(sorted);
        for (Map.Entry<String, Set<Command>> entry : releaseCommandsPerZone.entrySet()) {
            for (Command command : entry.getValue()) {
                commandMap.addCommand(entry.getKey(), command);
            }
        }

        return commandMap;
    }


    /**
     * Topologically sorts components according to their URI.
     *
     * @param components the collection to sort
     * @return a sorted collection
     */
    private List<LogicalComponent<?>> topologicalSort(Collection<LogicalComponent<?>> components) {
        List<LogicalComponent<?>> sorted = new ArrayList<LogicalComponent<?>>();
        for (LogicalComponent<?> component : components) {
            sorted.add(component);
            if (component instanceof LogicalCompositeComponent) {
                flatten((LogicalCompositeComponent) component, sorted);
            }
        }
        Collections.sort(sorted, COMPARATOR);
        return sorted;
    }

    /**
     * Recursively adds composite children to the collection of components
     *
     * @param component  the composite component
     * @param components the collection
     */
    private void flatten(LogicalCompositeComponent component, List<LogicalComponent<?>> components) {
        for (LogicalComponent<?> child : component.getComponents()) {
            components.add(child);
            if (child instanceof LogicalCompositeComponent) {
                flatten((LogicalCompositeComponent) child, components);
            }
        }

    }

    private List<CommandGenerator> sort(List<? extends CommandGenerator> commandGenerators) {
        Comparator<CommandGenerator> generatorComparator = new Comparator<CommandGenerator>() {

            public int compare(CommandGenerator first, CommandGenerator second) {
                return first.getOrder() - second.getOrder();
            }
        };
        List<CommandGenerator> sorted = new ArrayList<CommandGenerator>(commandGenerators);
        Collections.sort(sorted, generatorComparator);
        return sorted;
    }


}
