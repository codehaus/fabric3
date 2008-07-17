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
package org.fabric3.fabric.generator.classloader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.ProvisionClassloaderCommand;
import org.fabric3.fabric.runtime.ComponentNames;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionUriEncoder;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Default implementation of the ClassLoaderGenerator. Clasloader generation groups components contained in a composite or include in separate
 * classloaders. Specifically, this implementation generates PhysicalClassLoaderDefinitions in the following way:
 * <pre>
 * <ul>
 * <li>All contributions required by the set of logical components are analyzed and corresponding PhysicalClassLoaderDefinitions for the
 * contributions
 * and their imports are created.
 * </li>
 * <li> The set of logical componets are processed and PhysicalClassLoaderDefinitions are created for all classloader ids referenced by the logical
 * component. These definitions will have the PhysicalClassLoaderDefinitions generated in the first step set as parents for that correspond to
 * contributions required by a component.
 * </li>
 * </ul>
 */
public class ClassLoaderCommandGeneratorImpl implements ClassLoaderCommandGenerator {
    private MetaDataStore store;
    private ContributionUriEncoder encoder;

    public ClassLoaderCommandGeneratorImpl(@Reference MetaDataStore store) {
        this.store = store;
    }

    /**
     * Setter for injecting the ArtifactLocationEncoder. This is done lazily as the encoder is supplied by an extension which is intialized after the
     * ClassLoaderGenerator which is needed during bootstrap.
     *
     * @param encoders the encoder to inject
     */
    @Reference(required = false)
    public void setEncoder(List<ContributionUriEncoder> encoders) {
        if (encoders == null || encoders.isEmpty()) {
            return;
        }
        // workaround for FABRICTHREE-262: only multiplicity references can be reinjected
        this.encoder = encoders.get(0);
    }

    public Map<URI, Set<Command>> generate(List<LogicalComponent<?>> components) throws GenerationException {
        // contributions mapped to the node they are provisioned to
        Map<URI, Set<URI>> contributionsPerRuntime = collateContributions(components);
        Map<URI, Set<PhysicalClassLoaderDefinition>> definitionsPerRuntime = createContributionClassLoaderDefinitions(contributionsPerRuntime);
        createComponentClassLoaderDefinitions(components, definitionsPerRuntime);
        Map<URI, Set<Command>> commandsPerRuntime = new HashMap<URI, Set<Command>>(definitionsPerRuntime.size());
        for (Map.Entry<URI, Set<PhysicalClassLoaderDefinition>> entry : definitionsPerRuntime.entrySet()) {
            Set<PhysicalClassLoaderDefinition> definitions = entry.getValue();
            Set<Command> commands = new LinkedHashSet<Command>();
            commandsPerRuntime.put(entry.getKey(), commands);
            for (PhysicalClassLoaderDefinition definition : definitions) {
                commands.add(new ProvisionClassloaderCommand(0, definition));
            }
        }

        return commandsPerRuntime;
    }

    /**
     * Processes the component set and collates all required contributions by runtime id
     *
     * @param components the set of components
     * @return the set of required contribution URIs grouped by runtime id
     */
    private Map<URI, Set<URI>> collateContributions(List<LogicalComponent<?>> components) {
        // collate all contributions that must be provisioned as part of the change set
        Map<URI, Set<URI>> contributionsPerRuntime = new HashMap<URI, Set<URI>>();
        for (LogicalComponent<?> component : components) {
            URI contributionUri = component.getDefinition().getContributionUri();
            if (contributionUri != null) {
                // xcv FIXME need to deal with the case where a contribution has been imported by another and has not yet been provisioned.
                // xcv FIXME contribution URIs can be null if component is primordial. this should be fixed in the synthesize
                URI runtimeId = component.getRuntimeId();
                Set<URI> contributions = contributionsPerRuntime.get(runtimeId);
                if (contributions == null) {
                    contributions = new LinkedHashSet<URI>();
                    contributionsPerRuntime.put(runtimeId, contributions);
                }
                contributions.add(contributionUri);
            }
        }
        return contributionsPerRuntime;
    }

    /**
     * Creates PhysicalClassLoaderDefinitions for the set of contributions grouped by runtime id
     *
     * @param contributionsPerRuntime the contribution URIs grouped by runtime id
     * @return the PhysicalClassLoaderDefinitions grouped by runtime id
     * @throws GenerationException if a generation error occurs
     */
    private Map<URI, Set<PhysicalClassLoaderDefinition>> createContributionClassLoaderDefinitions(Map<URI, Set<URI>> contributionsPerRuntime)
            throws GenerationException {
        Map<URI, Set<PhysicalClassLoaderDefinition>> definitionsPerRuntime = new HashMap<URI, Set<PhysicalClassLoaderDefinition>>();
        for (Map.Entry<URI, Set<URI>> entry : contributionsPerRuntime.entrySet()) {
            for (URI uri : entry.getValue()) {
                Contribution contribution = store.find(uri);
                PhysicalClassLoaderDefinition definition = new PhysicalClassLoaderDefinition(uri);
                definition.addContributionUri(encode(uri));
                for (URI resolved : contribution.getResolvedImportUris()) {
                    definition.addParentClassLoader(resolved);
                }
                URI runtimeId = entry.getKey();
                Set<PhysicalClassLoaderDefinition> definitions = definitionsPerRuntime.get(runtimeId);
                if (definitions == null) {
                    definitions = new LinkedHashSet<PhysicalClassLoaderDefinition>();
                    definitionsPerRuntime.put(runtimeId, definitions);
                }
                definitions.add(definition);
            }

        }
        return definitionsPerRuntime;
    }

    /**
     * Processes a component set, updating the suppied collection of PhysicalClassLoaderDefinitions.
     *
     * @param components            the component set
     * @param definitionsPerRuntime the collection of PhysicalClassLoaderDefinitions to add to grouped by runtime id
     * @throws GenerationException if a generation error occurs
     */
    private void createComponentClassLoaderDefinitions(List<LogicalComponent<?>> components,
                                                       Map<URI, Set<PhysicalClassLoaderDefinition>> definitionsPerRuntime)
            throws GenerationException {
        for (LogicalComponent<?> component : components) {
            URI classLoaderUri = component.getClassLoaderId();
            // XCV FIXME classloaderID is null for system components passed by the runtime. need to set in bootstrapper
            if (classLoaderUri == null) {
                classLoaderUri = URI.create(ComponentNames.RUNTIME_NAME);
            }
            PhysicalClassLoaderDefinition definition = new PhysicalClassLoaderDefinition(classLoaderUri);
            LogicalComponent<CompositeImplementation> grandParent = component.getParent().getParent();
            if (grandParent != null) {
                // set the classloader hierarchy if we are not at the domain level
                URI uri = grandParent.getUri();
                definition.addParentClassLoader(uri);
            }
            URI contributionUri = component.getDefinition().getContributionUri();
            Set<PhysicalClassLoaderDefinition> definitions = definitionsPerRuntime.get(component.getRuntimeId());
            if (contributionUri == null) {
                // xcv FIXME bootstrap services should be associated with a contribution
                // the logical component is not provisioned as part of a contribution, e.g. a boostrap system service
                definition.addParentClassLoader(ComponentNames.BOOT_CLASSLOADER_ID);
                if (definitions == null) {
                    definitions = new LinkedHashSet<PhysicalClassLoaderDefinition>();
                    definitionsPerRuntime.put(component.getRuntimeId(), definitions);
                }
                definitions.add(definition);
                continue;
            }
            definition.addParentClassLoader(contributionUri);
            definitions.add(definition);
        }
    }

    private URI encode(URI uri) throws GenerationException {
        if (encoder != null) {
            try {
                return encoder.encode(uri);
            } catch (URISyntaxException e) {
                throw new GenerationException(e);
            }
        }
        return uri;


    }

}