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
import java.util.UUID;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.generator.classloader.ClassLoaderCommandGenerator;
import org.fabric3.fabric.generator.context.StartContextCommandGenerator;
import org.fabric3.fabric.generator.context.StopContextCommandGenerator;
import org.fabric3.fabric.generator.extension.ExtensionGenerator;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Default Generator implementation.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class GeneratorImpl implements Generator {
    private static final Comparator<LogicalComponent<?>> COMPARATOR = new Comparator<LogicalComponent<?>>() {
        public int compare(LogicalComponent<?> first, LogicalComponent<?> second) {
            return first.getUri().compareTo(second.getUri());
        }
    };

    private final List<CommandGenerator> commandGenerators;
    private ContributionCollator collator;
    private ClassLoaderCommandGenerator classLoaderCommandGenerator;
    private StartContextCommandGenerator startContextCommandGenerator;
    private StopContextCommandGenerator stopContextCommandGenerator;
    private ExtensionGenerator extensionGenerator;

    public GeneratorImpl(@Reference List<CommandGenerator> commandGenerators,
                         @Reference ContributionCollator collator,
                         @Reference ClassLoaderCommandGenerator classLoaderCommandGenerator,
                         @Reference StartContextCommandGenerator startContextCommandGenerator,
                         @Reference StopContextCommandGenerator stopContextCommandGenerator) {
        this.collator = collator;
        this.classLoaderCommandGenerator = classLoaderCommandGenerator;
        this.startContextCommandGenerator = startContextCommandGenerator;
        this.stopContextCommandGenerator = stopContextCommandGenerator;
        // sort the command generators
        this.commandGenerators = sort(commandGenerators);
    }

    /**
     * Lazily injected after bootstrap.
     *
     * @param extensionGenerator the extension  generator
     */
    @Reference(required = false)
    public void setExtensionGenerator(ExtensionGenerator extensionGenerator) {
        this.extensionGenerator = extensionGenerator;
    }

    public CommandMap generate(Collection<LogicalComponent<?>> components, boolean incremental) throws GenerationException {
        List<LogicalComponent<?>> sorted = topologicalSort(components);
        String id = UUID.randomUUID().toString();
        CommandMap commandMap = new CommandMap(id);
        Map<String, List<Contribution>> deployingContributions;
        if (incremental) {
            deployingContributions = collator.collateContributions(sorted, GenerationType.INCREMENTAL);
        } else {
            deployingContributions = collator.collateContributions(sorted, GenerationType.FULL);
        }

        // generate classloader provision commands
        Map<String, List<Command>> commandsPerZone = classLoaderCommandGenerator.generate(deployingContributions);
        for (Map.Entry<String, List<Command>> entry : commandsPerZone.entrySet()) {
            commandMap.addCommands(entry.getKey(), entry.getValue());
        }

        // generate stop context information
        Map<String, List<Command>> stopCommands = stopContextCommandGenerator.generate(sorted);
        for (Map.Entry<String, List<Command>> entry : stopCommands.entrySet()) {
            commandMap.addCommands(entry.getKey(), entry.getValue());
        }

        for (CommandGenerator generator : commandGenerators) {
            for (LogicalComponent<?> component : sorted) {
                Command command = generator.generate(component, incremental);
                if (command != null) {
                    if (commandMap.getZoneCommands(component.getZone()).getCommands().contains(command)) {
                        continue;
                    }
                    commandMap.addCommand(component.getZone(), command);
                }
            }
        }

        // start contexts
        Map<String, List<Command>> startCommands = startContextCommandGenerator.generate(sorted, commandMap, incremental);
        for (Map.Entry<String, List<Command>> entry : startCommands.entrySet()) {
            commandMap.addCommands(entry.getKey(), entry.getValue());
        }

        // release classloaders for components being undeployed that are no longer referenced
        Map<String, List<Contribution>> undeployingContributions = collator.collateContributions(sorted, GenerationType.UNDEPLOY);
        Map<String, List<Command>> releaseCommandsPerZone = classLoaderCommandGenerator.release(undeployingContributions);
        for (Map.Entry<String, List<Command>> entry : releaseCommandsPerZone.entrySet()) {
            commandMap.addCommands(entry.getKey(), entry.getValue());
        }

        // generate extension provision commands - this must be done after policies are calculated for policy extensions to be included
        if (incremental) {
            generateExtensionCommands(commandMap, deployingContributions, sorted, GenerationType.INCREMENTAL);
        } else {
            generateExtensionCommands(commandMap, deployingContributions, sorted, GenerationType.FULL);
        }
        // release extensions that are no longer used
        generateExtensionCommands(commandMap, undeployingContributions, sorted, GenerationType.UNDEPLOY);

        return commandMap;
    }

    /**
     * Generate extension provision commands for the contributions and components being deployed/undeployed
     *
     * @param commandMap             the map of commands for deployment
     * @param deployingContributions the contributions being deployed
     * @param components             the components being deployed
     * @param type                   the type of generation being performed
     * @throws GenerationException if an error during generation is encountered
     */
    private void generateExtensionCommands(CommandMap commandMap,
                                           Map<String, List<Contribution>> deployingContributions,
                                           List<LogicalComponent<?>> components,
                                           GenerationType type) throws GenerationException {
        if (extensionGenerator != null) {
            Map<String, Command> extensionsPerZone = extensionGenerator.generate(deployingContributions, components, commandMap, type);
            if (extensionsPerZone != null) {
                for (Map.Entry<String, Command> entry : extensionsPerZone.entrySet()) {
                    if (type == GenerationType.UNDEPLOY) {
                        commandMap.addCommand(entry.getKey(), entry.getValue());
                    } else {
                        // if an extension is being provisioned, the command needs to be executed before others
                        commandMap.addExtensionCommand(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
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
