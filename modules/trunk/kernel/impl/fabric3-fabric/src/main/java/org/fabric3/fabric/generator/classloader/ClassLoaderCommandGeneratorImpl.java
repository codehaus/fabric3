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

    public Map<String, Set<Command>> generate(List<LogicalComponent<?>> components) throws GenerationException {
        // contributions mapped to the node they are provisioned to
        Map<String, Set<URI>> contributionsPerZone = calculateCollatedContributions(components);
        Map<String, Set<PhysicalClassLoaderDefinition>> definitionsPerZone = createContributionClassLoaderDefinitions(contributionsPerZone);
        createComponentClassLoaderDefinitions(components, definitionsPerZone);
        Map<String, Set<Command>> commandsPerRuntime = new HashMap<String, Set<Command>>(definitionsPerZone.size());
        for (Map.Entry<String, Set<PhysicalClassLoaderDefinition>> entry : definitionsPerZone.entrySet()) {
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
     * Processes the list of components being deployed and returns the set of required contributions collated by zone.
     *
     * @param components the set of components
     * @return the set of required contribution URIs grouped by runtime id
     */
    private Map<String, Set<URI>> calculateCollatedContributions(List<LogicalComponent<?>> components) {
        // collate all contributions that must be provisioned as part of the change set
        Map<String, Set<URI>> contributionsPerZone = new HashMap<String, Set<URI>>();
        for (LogicalComponent<?> component : components) {
            URI contributionUri = component.getDefinition().getContributionUri();
            if (contributionUri != null) {
                // xcv FIXME contribution URIs can be null if component is primordial. this should be fixed in the synthesize
                String zone = component.getZone();
                Set<URI> contributions = contributionsPerZone.get(zone);
                if (contributions == null) {
                    contributions = new LinkedHashSet<URI>();
                    contributionsPerZone.put(zone, contributions);
                }
                Contribution contribution = store.find(contributionUri);
                // imported contributions must also be provisioned 
                for (URI uri : contribution.getResolvedImportUris()) {
                    // ignore the boot and application classloaders
                    if (ComponentNames.BOOT_CLASSLOADER_ID.equals(uri) || ComponentNames.APPLICATION_CLASSLOADER_ID.equals(uri)) {
                        continue;
                    }
                    if (!contributions.contains(uri)) {
                        contributions.add(uri);
                    }
                }
                contributions.add(contributionUri);
            }
        }
        return contributionsPerZone;
    }

    /**
     * Creates PhysicalClassLoaderDefinitions for the set of contributions grouped by runtime id
     *
     * @param contributionsPerZone the contribution URIs grouped by runtime id
     * @return the PhysicalClassLoaderDefinitions grouped by zone
     * @throws GenerationException if a generation error occurs
     */
    private Map<String, Set<PhysicalClassLoaderDefinition>> createContributionClassLoaderDefinitions(Map<String, Set<URI>> contributionsPerZone)
            throws GenerationException {
        Map<String, Set<PhysicalClassLoaderDefinition>> definitionsPerZone = new HashMap<String, Set<PhysicalClassLoaderDefinition>>();
        for (Map.Entry<String, Set<URI>> entry : contributionsPerZone.entrySet()) {
            for (URI uri : entry.getValue()) {
                String zone = entry.getKey();
                Contribution contribution = store.find(uri);
                PhysicalClassLoaderDefinition definition = new PhysicalClassLoaderDefinition(uri);
                if (zone == null) {
                    // If the contribution is providioned to this runtime, its URI should not be encoded
                    definition.addContributionUri(uri);
                } else {
                    URI encoded = encode(uri);
                    definition.addContributionUri(encoded);
                }
                for (URI resolved : contribution.getResolvedImportUris()) {
                    definition.addParentClassLoader(resolved);
                }
                Set<PhysicalClassLoaderDefinition> definitions = definitionsPerZone.get(zone);
                if (definitions == null) {
                    definitions = new LinkedHashSet<PhysicalClassLoaderDefinition>();
                    definitionsPerZone.put(zone, definitions);
                }
                definitions.add(definition);
            }

        }
        return definitionsPerZone;
    }

    /**
     * Processes a component set, updating the supplied collection of PhysicalClassLoaderDefinitions.
     *
     * @param components         the component set
     * @param definitionsPerZone the collection of PhysicalClassLoaderDefinitions to add to grouped by zone
     * @throws GenerationException if a generation error occurs
     */
    private void createComponentClassLoaderDefinitions(List<LogicalComponent<?>> components,
                                                       Map<String, Set<PhysicalClassLoaderDefinition>> definitionsPerZone)
            throws GenerationException {
        for (LogicalComponent<?> component : components) {
            URI classLoaderUri = component.getClassLoaderId();
            if (ComponentNames.BOOT_CLASSLOADER_ID.equals(classLoaderUri)) {
                // skip provisioning for the boot classloader
                continue;
            }
            PhysicalClassLoaderDefinition definition = new PhysicalClassLoaderDefinition(classLoaderUri);
            LogicalComponent<CompositeImplementation> grandParent = component.getParent().getParent();
            if (grandParent != null) {
                // set the classloader hierarchy if we are not at the domain level
                URI uri = grandParent.getUri();
                definition.addParentClassLoader(uri);
            }
            URI contributionUri = component.getDefinition().getContributionUri();
            Set<PhysicalClassLoaderDefinition> definitions = definitionsPerZone.get(component.getZone());
            if (contributionUri == null) {
                // xcv FIXME bootstrap services should be associated with a contribution
                // the logical component is not provisioned as part of a contribution, e.g. a boostrap system service
                definition.addParentClassLoader(ComponentNames.BOOT_CLASSLOADER_ID);
                if (definitions == null) {
                    definitions = new LinkedHashSet<PhysicalClassLoaderDefinition>();
                    definitionsPerZone.put(component.getZone(), definitions);
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