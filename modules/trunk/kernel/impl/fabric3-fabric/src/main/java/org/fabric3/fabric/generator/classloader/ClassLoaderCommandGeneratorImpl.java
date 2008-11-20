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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.ProvisionClassloaderCommand;
import org.fabric3.fabric.command.UnprovisionClassloaderCommand;
import org.fabric3.fabric.services.contribution.DependencyException;
import org.fabric3.fabric.services.contribution.DependencyService;
import org.fabric3.host.Names;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionUriEncoder;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Fabric3 may be configured to enforce a modular environment where classloader isolation is maintained between application and extension code. This
 * component generates classloader provision and unprovision commands used during deployment and undeployment that provide this isolation. The
 * classloader architecture for modular environments is described below.
 * <p/>
 * During deployment, components are associated with a classloader for their composite. Included composites have their own classloader. Contributions
 * required by components are loaded in their own classloaders. Composite classloaders are multiparent and have the contribution classloaders required
 * to run their components set as parents. All classloaders have a URI which correspnds either to the composite or contribution URI. In single-VM
 * domains, an optimization is made where contribution classloaders used by the contribution service infrastructure are reused during deployment and
 * visibile to the appropriate composite classloaders.
 * <p/>
 * During deployment, classloader definitions for contributions (and their transitive imports) required by components and composite classloaders are
 * generated. These definitions are used by classloader provision commands which are sent to the zones where the components are deployed.
 * <p/>
 * During undeployment, the process is reversed. Commands for releasing composite classloaders and contribution classloaders are sent to the zones
 * where components are being undeployed. Invidual zones and runtimes are responsible for deciding when to dispose of released classloaders. For
 * example, a contribution classloader used by two composites that is released when one composite is undeployed will not be removed until both
 * composites are undeployed.
 */
public class ClassLoaderCommandGeneratorImpl implements ClassLoaderCommandGenerator {
    private MetaDataStore store;
    private ContributionUriEncoder encoder;
    private DependencyService dependencyService;

    public ClassLoaderCommandGeneratorImpl(@Reference MetaDataStore store) {
        this.store = store;
    }

    /**
     * Setter for injecting the service for encoding contribution URIs so they may be derferenced in a domain. This is done lazily as the encoder is
     * supplied by an extension which is intialized after this component which is needed during bootstrap.
     *
     * @param encoder the encoder to inject
     */
    @Reference(required = false)
    public void setEncoder(ContributionUriEncoder encoder) {
        this.encoder = encoder;
    }

    /**
     * Setter to allow lazy injection of the dependency service. This is used for undeployment only, which is not required during bootstrap.
     *
     * @param dependencyService the dependency service
     */
    @Reference(required = false)
    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public Map<String, Set<Command>> generate(List<LogicalComponent<?>> components) throws GenerationException {
        // commands mapped to zone
        Map<String, Set<Contribution>> collated = collateContributions(components, LogicalState.NEW);

        // Create the classloader definitions for contributions required to run the components being deployed.
        // These are created first since they must be instantitated on a runtime prior to component classloaders
        Map<String, Set<PhysicalClassLoaderDefinition>> definitionsPerZone = createContributionDefinitions(collated);
        createComponentDefinitions(components, definitionsPerZone);
        return createProvisionCommands(definitionsPerZone);
    }

    public Map<String, Set<Command>> release(List<LogicalComponent<?>> components) throws GenerationException {
        // commands mapped to the zone
        Map<String, Set<Command>> commandsPerZone = new HashMap<String, Set<Command>>();

        // generate commands to unprovision component classloaders
        for (LogicalComponent<?> component : components) {
            URI classLoaderUri = component.getClassLoaderId();
            if (LogicalState.MARKED != component.getState() || Names.BOOT_CLASSLOADER_ID.equals(classLoaderUri)) {
                // skip provisioning for previously provisioned components and the boot classloader
                continue;
            }
            UnprovisionClassloaderCommand command = new UnprovisionClassloaderCommand(7, classLoaderUri);
            Set<Command> commands = commandsPerZone.get(component.getZone());
            if (commands == null) {
                commands = new LinkedHashSet<Command>();
                commandsPerZone.put(component.getZone(), commands);
            }
            commands.add(command);
        }

        Map<String, Set<Contribution>> collated = collateContributions(components, LogicalState.MARKED);

        // generate commands to unprovision contribution classloaders
        for (Map.Entry<String, Set<Contribution>> entry : collated.entrySet()) {
            if (entry.getKey() == null) {
                // Don't uprovision the contribution classloader for locally deployed components since it is shared by the contrbution service
                // In a multi-VM domain, the contribution classloaders are unprovisioned when they are no longer referenced by component classloaders.
                // However, in a single-VM domain, the contribution classloader is shared by the contribution service infrastucture
                // (contributions are loaded in memory via a classloader) and any component classloaders that require the contribution.
                // Consequently, the contribution classloader cannot be removed until the contribution is uninstalled.
                continue;
            }
            Set<Command> commands = commandsPerZone.get(entry.getKey());
            if (commands == null) {
                commands = new LinkedHashSet<Command>();
                commandsPerZone.put(entry.getKey(), commands);
            }
            List<Contribution> ordered;
            try {
                // Contribution classloaders must be removed in reverse order of their dependencies.
                // Order the contributions by dependencies and reverse it to determine the sequence the classloaders must be removed in
                // Ordering is important for classloaders to be properly disposed. Runtimes will only dispose classloaders when they are no longer
                // referenced by other registered classloaders. This requires dependent classloaders to be released first.
                ordered = dependencyService.order(new ArrayList<Contribution>(entry.getValue()));
                Collections.reverse(ordered);
            } catch (DependencyException e) {
                throw new GenerationException(e);
            }
            for (Contribution contribution : ordered) {
                UnprovisionClassloaderCommand command = new UnprovisionClassloaderCommand(7, contribution.getUri());
                commands.add(command);
            }
        }

        return commandsPerZone;
    }

    /**
     * Collates contributions for components being deployed or undeployed by zone. That is, the list of components is processed to determine the
     * required set of contributions keyed by zone where the components are to be deployed to or undeployed from.
     *
     * @param components the set of components
     * @param state      the logical state. Either LogicalState#NEW or LogicalState#MARKED representing deploy and undeploy respectively
     * @return the set of required contributions grouped by zone
     */
    private Map<String, Set<Contribution>> collateContributions(List<LogicalComponent<?>> components, LogicalState state) {
        // collate all contributions that must be provisioned as part of the change set
        Map<String, Set<Contribution>> contributionsPerZone = new HashMap<String, Set<Contribution>>();
        for (LogicalComponent<?> component : components) {
            if (state != component.getState()) {
                continue;
            }
            URI contributionUri = component.getDefinition().getContributionUri();
            if (contributionUri != null) {
                // xcv FIXME contribution URIs can be null if component is primordial. this should be fixed in the synthesize
                String zone = component.getZone();
                Set<Contribution> contributions = contributionsPerZone.get(zone);
                if (contributions == null) {
                    contributions = new LinkedHashSet<Contribution>();
                    contributionsPerZone.put(zone, contributions);
                }
                Contribution contribution = store.find(contributionUri);
                // imported contributions must also be provisioned 
                for (URI uri : contribution.getResolvedImportUris()) {
                    // ignore the boot and application classloaders
                    if (Names.BOOT_CLASSLOADER_ID.equals(uri) || Names.APPLICATION_CLASSLOADER_ID.equals(uri)) {
                        continue;
                    }
                    Contribution imported = store.find(uri);
                    if (!contributions.contains(imported)) {
                        contributions.add(imported);
                    }
                }
                contributions.add(contribution);
            }
        }
        return contributionsPerZone;
    }

    /**
     * Creates classloader definitions for a set of contributions grouped by zone id
     *
     * @param contributionsPerZone the contributions grouped by zone id
     * @return the PhysicalClassLoaderDefinitions grouped by zone
     * @throws GenerationException if a generation error occurs
     */
    private Map<String, Set<PhysicalClassLoaderDefinition>> createContributionDefinitions(Map<String, Set<Contribution>> contributionsPerZone)
            throws GenerationException {
        Map<String, Set<PhysicalClassLoaderDefinition>> definitionsPerZone = new HashMap<String, Set<PhysicalClassLoaderDefinition>>();
        for (Map.Entry<String, Set<Contribution>> entry : contributionsPerZone.entrySet()) {
            for (Contribution contribution : entry.getValue()) {
                String zone = entry.getKey();
                URI uri = contribution.getUri();
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
     * Creates classloader definitions required to run a set of components and adds them to the supplied collection of
     * PhysicalClassLoaderDefinitions.
     *
     * @param components         the component set
     * @param definitionsPerZone the collection of PhysicalClassLoaderDefinitions keyed by zone
     * @throws GenerationException if a generation error occurs
     */
    private void createComponentDefinitions(List<LogicalComponent<?>> components,
                                            Map<String, Set<PhysicalClassLoaderDefinition>> definitionsPerZone) throws GenerationException {
        for (LogicalComponent<?> component : components) {
            URI classLoaderUri = component.getClassLoaderId();
            if (LogicalState.NEW != component.getState() || Names.BOOT_CLASSLOADER_ID.equals(classLoaderUri)) {
                // skip provisioning for previously provisioned components and the boot classloader
                continue;
            }
            PhysicalClassLoaderDefinition definition = new PhysicalClassLoaderDefinition(classLoaderUri);
            URI contributionUri = component.getDefinition().getContributionUri();
            Set<PhysicalClassLoaderDefinition> definitions = definitionsPerZone.get(component.getZone());
            if (contributionUri == null) {
                definition.addParentClassLoader(Names.BOOT_CLASSLOADER_ID);
                // xcv FIXME bootstrap services should be associated with a contribution
                // the logical component is not provisioned as part of a contribution, e.g. a boostrap system service
                if (definitions == null) {
                    definitions = new LinkedHashSet<PhysicalClassLoaderDefinition>();
                    definitionsPerZone.put(component.getZone(), definitions);
                }
                definitions.add(definition);
            } else {
                definition.addParentClassLoader(contributionUri);
                definitions.add(definition);
            }
        }
    }

    /**
     * Creates classloader provision commands for a set of classloader definitions
     *
     * @param definitionsPerZone the classloader definitions keyed by zone
     * @return the set of commands keyed by zone
     */
    private Map<String, Set<Command>> createProvisionCommands(Map<String, Set<PhysicalClassLoaderDefinition>> definitionsPerZone) {
        Map<String, Set<Command>> commandsPerZone = new HashMap<String, Set<Command>>();
        for (Map.Entry<String, Set<PhysicalClassLoaderDefinition>> entry : definitionsPerZone.entrySet()) {
            Set<PhysicalClassLoaderDefinition> definitions = entry.getValue();
            Set<Command> commands = new LinkedHashSet<Command>();
            commandsPerZone.put(entry.getKey(), commands);
            for (PhysicalClassLoaderDefinition definition : definitions) {
                commands.add(new ProvisionClassloaderCommand(0, definition));
            }
        }
        return commandsPerZone;
    }

    /**
     * Encodes a contribution URI to one that is derferenceable from a runtime in the domain
     *
     * @param uri the contribution URI
     * @return a URI that is derferenceable in the domain
     * @throws GenerationException if the URI cannot be encoded
     */
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