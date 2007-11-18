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
package org.fabric3.fabric.model.physical;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.fabric.assembly.resolver.ResolutionException;
import org.fabric3.fabric.command.InitializeComponentCommand;
import org.fabric3.fabric.generator.DefaultGeneratorContext;
import org.fabric3.fabric.runtime.ComponentNames;
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.assembly.AssemblyStore;
import org.fabric3.spi.assembly.RecoveryException;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CommandSet;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.model.physical.PhysicalModelGenerator;
import org.fabric3.spi.util.UriHelper;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Default implementation of the physical model generator.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class PhysicalModelGeneratorImpl implements PhysicalModelGenerator {

    private final GeneratorRegistry generatorRegistry;
    private final AssemblyStore assemblyStore;
    
    private LogicalComponent<CompositeImplementation> domain;

    /**
     * Injects generator registry and assembly store.
     * 
     * @param generatorRegistry Generator registry.
     * @param assemblyStore Assembly store.
     */
    public PhysicalModelGeneratorImpl(@Reference GeneratorRegistry generatorRegistry,
                                      @Reference AssemblyStore assemblyStore) {
        this.generatorRegistry = generatorRegistry;
        this.assemblyStore = assemblyStore;
    }
    
    /**
     * Reads the runtime domain from the assembly store.
     * 
     * @throws RecoveryException
     */
    @Init
    public void initialize() {
        try {
            domain = assemblyStore.read();
        } catch(RecoveryException ex) {
            throw new AssertionError(ex);
        }
    }

    /**
     * Generates the physical component definitions from the logical components.
     */
    public Map<URI, GeneratorContext> generate(Collection<LogicalComponent<?>> components) throws ActivateException {

        Map<URI, GeneratorContext> contexts = new HashMap<URI, GeneratorContext>();

        try {

            // Generate the change sets
            for (LogicalComponent<?> component : components) {
                generateChangeSets(component, contexts);
            }

            // Generate the command sets
            for (LogicalComponent<?> component : components) {
                generateCommandSets(component, contexts);
            }

        } catch (GenerationException e) {
            throw new ActivateException(e);
        } catch (ResolutionException e) {
            throw new ActivateException(e);
        }

        return contexts;

    }

    private void generateChangeSets(LogicalComponent<?> component, Map<URI, GeneratorContext> contexts)
            throws GenerationException, ResolutionException {

        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();
        Implementation<?> implementation = definition.getImplementation();

        if (CompositeImplementation.IMPLEMENTATION_COMPOSITE.equals(implementation.getType())) {
            for (LogicalComponent<?> child : component.getComponents()) {
                // if the component is already running on a node (e.g. during recovery), skip provisioning
                if (child.isActive()) {
                    continue;
                }
                // generate changesets recursively for children
                generateChangeSets(child, contexts);
            }
        } else {
            // leaf component, generate a physical component and update the change sets
            // if component is already running on a node (e.g. during recovery), skip provisioning
            if (component.isActive()) {
                return;
            }
            generatePhysicalComponent(component, contexts);
            generatePhysicalWires(component, contexts);
        }

    }

    private void generatePhysicalWires(LogicalComponent<?> component, Map<URI, GeneratorContext> contexts)
            throws GenerationException, ResolutionException {

        URI runtimeId = component.getRuntimeId();
        GeneratorContext context = contexts.get(runtimeId);

        if (context == null) {
            PhysicalChangeSet changeSet = new PhysicalChangeSet();
            CommandSet commandSet = new CommandSet();
            context = new DefaultGeneratorContext(changeSet, commandSet);
            contexts.put(runtimeId, context);
        }

        for (LogicalReference entry : component.getReferences()) {
            if (entry.getBindings().isEmpty()) {
                for (URI uri : entry.getTargetUris()) {
                    LogicalComponent<?> target = findComponent(uri);
                    if(target == null) {
                        System.err.println("Unable to find target for " + uri);
                    }
                    String serviceName = uri.getFragment();
                    LogicalService targetService = target.getService(serviceName);
                    assert targetService != null;
                    while (CompositeImplementation.class.isInstance(target.getDefinition().getImplementation())) {
                        URI promoteUri = targetService.getPromote();
                        URI promotedComponent = UriHelper.getDefragmentedName(promoteUri);
                        target = target.getComponent(promotedComponent);
                        targetService = target.getService(promoteUri.getFragment());
                    }
                    LogicalReference reference = component.getReference(entry.getUri().getFragment());

                    generatorRegistry.generateUnboundWire(component, reference, targetService, target, context);

                }
            } else {
                // TODO this should be extensible and moved out
                LogicalBinding<?> logicalBinding = entry.getBindings().get(0);
                generatorRegistry.generateBoundReferenceWire(component, entry, logicalBinding, context);
            }

        }

        // generate changesets for bound service wires
        for (LogicalService service : component.getServices()) {
            List<LogicalBinding<?>> bindings = service.getBindings();
            if (bindings.isEmpty()) {
                // service is not bound, skip
                continue;
            }
            for (LogicalBinding<?> binding : service.getBindings()) {
                generatorRegistry.generateBoundServiceWire(service, binding, component, context);
            }
        }

        // generate wire definitions for resources
        for (LogicalResource<?> resource : component.getResources()) {
            generatorRegistry.generateResourceWire(component, resource, context);
        }

    }

    private void generateCommandSets(LogicalComponent<?> component, Map<URI, GeneratorContext> contexts)
            throws GenerationException {

        GeneratorContext context = contexts.get(component.getRuntimeId());
        if (context != null) {
            generatorRegistry.generateCommandSet(component, context);
            if (isEagerInit(component)) {
                // if the component is eager init, add it to the list of components to initialize on the node it
                // will be provisioned to
                CommandSet commandSet = context.getCommandSet();
                List<Command> set = commandSet.getCommands(CommandSet.Phase.LAST);
                boolean found = false;
                for (Command command : set) {
                    // check if the command exists, and if so update it
                    if (command instanceof InitializeComponentCommand) {
                        ((InitializeComponentCommand) command).addUri(component.getUri());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // a previous command was not found so create one
                    // @FIXME a trailing slash is needed since group ids are set on ComponentDefinitions using URI#resolve(",")
                    URI groupId = URI.create(component.getParent().getUri().toString() + "/");
                    InitializeComponentCommand initCommand = new InitializeComponentCommand(groupId);
                    initCommand.addUri(component.getUri());
                    commandSet.add(CommandSet.Phase.LAST, initCommand);
                }
            }
        }
        for (LogicalComponent<?> child : component.getComponents()) {
            generateCommandSets(child, contexts);
        }
    }

    private void generatePhysicalComponent(LogicalComponent<?> component, Map<URI, GeneratorContext> contexts)
            throws GenerationException {
        
        URI id = component.getRuntimeId();
        GeneratorContext context = contexts.get(id);
        if (context == null) {
            PhysicalChangeSet changeSet = new PhysicalChangeSet();
            CommandSet commandSet = new CommandSet();
            context = new DefaultGeneratorContext(changeSet, commandSet);
            contexts.put(id, context);
        }
        context.getPhysicalChangeSet().addComponentDefinition(
                generatorRegistry.generatePhysicalComponent(component, context));
        
    }

    private boolean isEagerInit(LogicalComponent<?> component) {
        
        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();
        AbstractComponentType<?, ?, ?, ?> componentType = definition.getImplementation().getComponentType();
        if (!componentType.getImplementationScope().equals(Scope.COMPOSITE)) {
            return false;
        }

        Integer level = definition.getInitLevel();
        if (level == null) {
            level = componentType.getInitLevel();
        }
        return level > 0;
        
    }

    private LogicalComponent<?> findComponent(URI uri) {
        
        String defragmentedUri = UriHelper.getDefragmentedNameAsString(uri);
        String domainString = ComponentNames.RUNTIME_URI.toString();
        String[] hierarchy = defragmentedUri.substring(domainString.length() + 1).split("/");
        String currentUri = domainString;
        LogicalComponent<?> currentComponent = domain;
        for (String name : hierarchy) {
            currentUri = currentUri + "/" + name;
            currentComponent = currentComponent.getComponent(URI.create(currentUri));
            if (currentComponent == null) {
                return null;
            }
        }
        return currentComponent;
        
    }

}
