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
package org.fabric3.fabric.domain;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.fabric.binding.BindingSelector;
import org.fabric3.fabric.collector.Collector;
import org.fabric3.fabric.generator.Generator;
import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.StoreException;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.CompositeAlreadyDeployedException;
import org.fabric3.host.domain.ContributionNotInstalledException;
import org.fabric3.host.domain.DeployableNotFoundException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.domain.UndeploymentException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.Include;
import org.fabric3.spi.allocator.AllocationException;
import org.fabric3.spi.allocator.Allocator;
import org.fabric3.spi.binding.BindingSelectionException;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.domain.DomainListener;
import org.fabric3.spi.domain.RoutingException;
import org.fabric3.spi.domain.RoutingService;
import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.ZoneCommands;
import org.fabric3.spi.model.instance.CopyUtil;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.plan.DeploymentPlan;
import org.fabric3.spi.services.lcm.LogicalComponentManager;
import org.fabric3.spi.services.lcm.WriteException;

/**
 * Base class for a domain.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractDomain implements Domain {
    private static final String PLAN_NAMESPACE = "urn:fabric3.org:extension:plan";

    protected RoutingService routingService;
    protected Generator generator;
    // The service for allocating to remote zones. Domain subtypes may optionally inject this service if they support distributed domains.
    protected Allocator allocator;
    protected List<DomainListener> listeners;

    private MetaDataStore metadataStore;
    private LogicalComponentManager logicalComponentManager;
    private LogicalModelInstantiator logicalModelInstantiator;
    private BindingSelector bindingSelector;
    private Collector collector;
    private HostInfo info;

    /**
     * Constructor.
     *
     * @param metadataStore            the store for resolving contribution artifacts
     * @param logicalComponentManager  the manager for logical components
     * @param generator                the physical model generator
     * @param logicalModelInstantiator the logical model instantiator
     * @param bindingSelector          the selector for binding.sca
     * @param routingService           the service for routing deployment commands
     * @param collector                the collector for undeploying componentsco
     * @param info                     the host info
     */
    public AbstractDomain(MetaDataStore metadataStore,
                          LogicalComponentManager logicalComponentManager,
                          Generator generator,
                          LogicalModelInstantiator logicalModelInstantiator,
                          BindingSelector bindingSelector,
                          RoutingService routingService,
                          Collector collector,
                          HostInfo info) {
        this.metadataStore = metadataStore;
        this.generator = generator;
        this.logicalModelInstantiator = logicalModelInstantiator;
        this.logicalComponentManager = logicalComponentManager;
        this.bindingSelector = bindingSelector;
        this.routingService = routingService;
        this.collector = collector;
        this.info = info;
        listeners = Collections.emptyList();
    }

    public void include(QName deployable) throws DeploymentException {
        include(deployable, null, false);
    }

    public void include(QName deployable, boolean transactional) throws DeploymentException {
        include(deployable, null, transactional);
    }

    public void include(QName deployable, String plan) throws DeploymentException {
        include(deployable, plan, false);
    }

    public void include(QName deployable, String plan, boolean transactional) throws DeploymentException {
        Composite composite = resolveComposite(deployable);
        // In order to include a composite at the domain level, it must first be wrapped in a composite that includes it.
        // This wrapper is thrown away during the inclusion.
        Composite wrapper = new Composite(deployable);
        Include include = new Include();
        include.setName(deployable);
        include.setIncluded(composite);
        wrapper.add(include);
        if (plan == null) {
            DeploymentPlan deploymentPlan = null;
            if (RuntimeMode.CONTROLLER == info.getRuntimeMode()) {
                // default to first found deployment plan in a contribution if none specifed for a distributed deployment
                Contribution contribution = metadataStore.resolveContainingContribution(new QNameSymbol(deployable));
                for (Resource resource : contribution.getResources()) {
                    for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                        if (element.getValue() instanceof DeploymentPlan) {
                            deploymentPlan = (DeploymentPlan) element.getValue();
                            break;
                        }
                    }
                }
            }
            include(wrapper, deploymentPlan, transactional);
        } else {
            DeploymentPlan deploymentPlan = resolvePlan(plan);
            include(wrapper, deploymentPlan, transactional);
        }
        for (DomainListener listener : listeners) {
            listener.onInclude(deployable, plan);
        }

    }

    public void include(Composite composite) throws DeploymentException {
        include(composite, null, false);
        for (DomainListener listener : listeners) {
            listener.onInclude(composite.getName(), null);
        }
    }

    public void include(List<URI> uris, boolean transactional) throws DeploymentException {
        Set<Contribution> contributions = resolveContributions(uris);
        instantiateAndDeploy(contributions, null, false, transactional);
    }


    public void undeploy(QName deployable) throws UndeploymentException {
        undeploy(deployable, false);
    }

    public void undeploy(QName deployable, boolean transactional) throws UndeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        if (transactional) {
            domain = CopyUtil.copy(domain);
        }
        collector.markForCollection(deployable, domain);
        try {
            CommandMap commandMap = generator.generate(domain.getComponents(), true);
            routingService.route(commandMap);
        } catch (GenerationException e) {
            throw new UndeploymentException("Error undeploying: " + deployable, e);
        } catch (RoutingException e) {
            throw new UndeploymentException("Error undeploying: " + deployable, e);

        }
        try {
            // TODO this should happen after nodes have undeployed the components and wires
            collector.collect(domain);
            logicalComponentManager.replaceRootComponent(domain);
            QNameSymbol deployableSymbol = new QNameSymbol(deployable);
            Contribution contribution = metadataStore.resolveContainingContribution(deployableSymbol);
            contribution.releaseLock(deployable);
            for (DomainListener listener : listeners) {
                listener.onUndeploy(deployable);
            }
        } catch (WriteException e) {
            throw new UndeploymentException("Error applying undeployment: " + deployable, e);
        }
    }

    public void recover(List<QName> deployables, List<String> planNames) throws DeploymentException {
        Set<Contribution> contributions = new LinkedHashSet<Contribution>();
        for (QName deployable : deployables) {
            QNameSymbol symbol = new QNameSymbol(deployable);
            Contribution contribution = metadataStore.resolveContainingContribution(symbol);
            if (contribution == null) {
                // this should not happen
                throw new DeploymentException("Contribution for deployable not found: " + deployable);
            }
            contributions.add(contribution);
        }
        instantiateAndDeploy(contributions, planNames, true, false);
    }

    public void recover(List<URI> uris) throws DeploymentException {
        Set<Contribution> contributions = resolveContributions(uris);
        instantiateAndDeploy(contributions, null, true, false);
    }

    public void regenerate(String zoneId, String correlationId) throws DeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();
        Collection<LogicalComponent<?>> components = domain.getComponents();
        try {
            CommandMap commandMap = generator.generate(components, false);
            ZoneCommands zoneCommands = commandMap.getZoneCommands(zoneId);
            List<Command> extensionCommands = zoneCommands.getExtensionCommands();
            List<Command> commands = zoneCommands.getCommands();
            CommandMap filtered = new CommandMap(commandMap.getId(), correlationId, true);
            filtered.addExtensionCommands(zoneId, extensionCommands);
            filtered.addCommands(zoneId, commands);
            routingService.route(filtered);
        } catch (GenerationException e) {
            throw new DeploymentException(e);
        } catch (RoutingException e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Instantiates and optionally deploys all deployables from a set of contributions. Deployment is performed if recovery mode is false or the
     * runtme is operating in single VM mode. When recovering in a distributed domain, the components contained in the deployables will be
     * instantiated but not deployed to zones. This is because the domain can run headless (i.e. without a controller) and may already be hosting
     * deployed components. In the case where a recovery is performed for the entire domain, including zones, the controller will instantiate
     * components and zone managers will send synchronization requests to it, which will result in component deployments.
     *
     * @param contributions the contributions to deploy
     * @param planNames     the deployment plan names or null if no deployment plans are specified. If running in a distributed domain and no plans
     *                      are specified, the contributions will be introspected for deployment plans.
     * @param recover       true if recovery mode is enabled
     * @param transactional true if the deployment should be performed transactionally
     * @throws DeploymentException if an error occurs during instantiation or deployment
     */
    private void instantiateAndDeploy(Set<Contribution> contributions, List<String> planNames, boolean recover, boolean transactional)
            throws DeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        for (Contribution contribution : contributions) {
            if (ContributionState.INSTALLED != contribution.getState()) {
                throw new ContributionNotInstalledException("Contribution is not installed: " + contribution.getUri());
            }
        }

        List<Composite> deployables = getDeployables(contributions);

        List<DeploymentPlan> plans;
        if (planNames == null) {
            plans = getDeploymentPlans(contributions);
        } else {
            plans = new ArrayList<DeploymentPlan>();
            for (String planName : planNames) {
                if (planName == null) {
                    plans.add(null);
                    continue;
                }
                DeploymentPlan plan = resolvePlan(planName);
                if (plan == null) {
                    // this should not happen
                    throw new DeploymentException("Deployment plan not found: " + planName);
                }
                plans.add(plan);
            }
        }
        // lock the contributions
        for (Contribution contribution : contributions) {
            for (Deployable deployable : contribution.getManifest().getDeployables()) {
                QName name = deployable.getName();
                // check if the deployable has already been deployed by querying the lock owners
                if (contribution.getLockOwners().contains(name)) {
                    throw new CompositeAlreadyDeployedException("Composite has already been deployed: " + name);
                }
                contribution.acquireLock(name);
            }
        }

        try {
            if (transactional) {
                domain = CopyUtil.copy(domain);
            }
            InstantiationContext change = logicalModelInstantiator.include(domain, deployables);
            if (change.hasErrors()) {
                throw new AssemblyException(change.getErrors());
            }
            if (!recover || RuntimeMode.VM == info.getRuntimeMode()) {
                // in single VM mode, recovery includes deployment
                allocateAndDeploy(domain, plans);
            } else {
                Collection<LogicalComponent<?>> components = domain.getComponents();
                allocate(components, plans);
                // Select bindings
                selectBinding(components);
                collector.markAsProvisioned(domain);
                logicalComponentManager.replaceRootComponent(domain);
            }

            // notify listeners
            for (int i = 0; i < deployables.size(); i++) {
                Composite deployable = deployables.get(i);
                String planName = null;
                if (!plans.isEmpty()) {
                    // deployment plans are not used in single-VM runtimes
                    planName = plans.get(i).getName();
                }
                for (DomainListener listener : listeners) {
                    listener.onInclude(deployable.getName(), planName);
                }
            }
        } catch (DeploymentException e) {
            // release the contribution locks if there was an error
            releaseLocks(contributions);
            throw e;
        } catch (AllocationException e) {
            // release the contribution locks if there was an error
            releaseLocks(contributions);
            throw new DeploymentException("Error deploying composite", e);
        } catch (WriteException e) {
            // release the contribution locks if there was an error
            releaseLocks(contributions);
            throw new DeploymentException("Error deploying composite", e);
        }
    }

    /**
     * Releases locks held on a set of contributions. Called when an error is raised during deployment causing a rollback.
     *
     * @param contributions the contributions to release locks on
     */
    private void releaseLocks(Set<Contribution> contributions) {
        for (Contribution contribution : contributions) {
            for (Deployable deployable : contribution.getManifest().getDeployables()) {
                QName name = deployable.getName();
                if (contribution.getLockOwners().contains(name)) {
                    contribution.releaseLock(name);
                }
            }
        }
    }

    /**
     * Includes a composite in the domain composite.
     *
     * @param composite     the composite to include
     * @param plan          the deployment plan to use or null
     * @param transactional if the inclusion should be performed transactionally
     * @throws DeploymentException if a deployment error occurs
     */
    private void include(Composite composite, DeploymentPlan plan, boolean transactional) throws DeploymentException {
        List<DeploymentPlan> plans;
        if (plan != null) {
            plans = new ArrayList<DeploymentPlan>();
            plans.add(plan);
        } else {
            plans = Collections.emptyList();
        }
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        QName name = composite.getName();
        Contribution contribution = metadataStore.resolveContainingContribution(new QNameSymbol(name));
        if (contribution != null && ContributionState.INSTALLED != contribution.getState()) {
            // a composite may not be associated with a contribution, e.g. a bootstrap composite
            throw new ContributionNotInstalledException("Contribution is not installed: " + contribution.getUri());
        }

        try {
            if (contribution != null) {
                // check if the deployable has already been deployed by querying the lock owners
                if (contribution.getLockOwners().contains(name)) {
                    throw new CompositeAlreadyDeployedException("Composite has already been deployed: " + name);
                }
                // lock the contribution
                contribution.acquireLock(name);
            }

            if (transactional) {
                domain = CopyUtil.copy(domain);
            }
            InstantiationContext context = logicalModelInstantiator.include(domain, composite);
            if (context.hasErrors()) {
                throw new AssemblyException(context.getErrors());
            }
            allocateAndDeploy(domain, plans);
        } catch (DeploymentException e) {
            // release the contribution lock if there was an error
            if (contribution != null && contribution.getLockOwners().contains(name)) {
                contribution.releaseLock(name);
            }
            throw e;
        }
    }

    /**
     * Resolves a deployment plan by name.
     *
     * @param plan the deployment plan name
     * @return the resolved deployment plan
     * @throws DeploymentException if the plan cannot be resolved
     */
    private DeploymentPlan resolvePlan(String plan) throws DeploymentException {
        ResourceElement<QNameSymbol, ?> element;
        DeploymentPlan deploymentPlan;
        try {
            QName planName = new QName(PLAN_NAMESPACE, plan);
            element = metadataStore.resolve(new QNameSymbol(planName));
        } catch (StoreException e) {
            throw new DeploymentException("Error finding plan: " + plan, e);
        }
        if (element == null) {
            throw new DeployableNotFoundException("Plan not found: " + plan, plan);
        }

        Object object = element.getValue();
        if (!(object instanceof DeploymentPlan)) {
            throw new IllegalDeployableTypeException("Not a deployment plan:" + plan, plan);
        }

        deploymentPlan = (DeploymentPlan) object;
        return deploymentPlan;
    }

    /**
     * Resolves a deployable by name.
     *
     * @param deployable the deployable name
     * @return the deployable
     * @throws DeploymentException if the deployable cannot be resolved
     */
    private Composite resolveComposite(QName deployable) throws DeploymentException {
        ResourceElement<QNameSymbol, ?> element;
        try {
            element = metadataStore.resolve(new QNameSymbol(deployable));
        } catch (StoreException e) {
            throw new DeploymentException("Error deploying: " + deployable, e);
        }
        if (element == null) {
            String id = deployable.toString();
            throw new DeployableNotFoundException("Deployable not found: " + id, id);
        }

        Object object = element.getValue();
        if (!(object instanceof Composite)) {
            String id = deployable.toString();
            throw new IllegalDeployableTypeException("Deployable must be a composite:" + id, id);
        }

        return (Composite) object;
    }

    /**
     * Allocates and deploys new components in the domain.
     *
     * @param domain the domain component
     * @param plans  the deployment plans to use for deployment
     * @throws DeploymentException if an error is encountered during deployment
     */
    private void allocateAndDeploy(LogicalCompositeComponent domain, List<DeploymentPlan> plans) throws DeploymentException {
        Collection<LogicalComponent<?>> components = domain.getComponents();
        // Allocate the components to runtime nodes
        try {
            allocate(components, plans);
        } catch (AllocationException e) {
            throw new DeploymentException("Error deploying composite", e);
        }

        // Select bindings
        selectBinding(components);
        try {
            // generate and provision any new components and new wires
            CommandMap commandMap = generator.generate(components, true);
            routingService.route(commandMap);
        } catch (GenerationException e) {
            throw new DeploymentException("Error deploying components", e);
        } catch (RoutingException e) {
            throw new DeploymentException("Error deploying components", e);
        }

        try {
            // TODO this should happen after nodes have deployed the components and wires
            collector.markAsProvisioned(domain);
            logicalComponentManager.replaceRootComponent(domain);
        } catch (WriteException e) {
            throw new DeploymentException("Error applying deployment", e);
        }
    }

    /**
     * Delegates to the Allocator to determine which runtimes to deploy the given collection of components to.
     *
     * @param components the components to allocate
     * @param plans      the deployment plans to use for allocation
     * @throws AllocationException if an allocation error occurs
     */
    private void allocate(Collection<LogicalComponent<?>> components, List<DeploymentPlan> plans) throws AllocationException {
        if (allocator == null) {
            // allocator is an optional extension
            return;
        }
        for (LogicalComponent<?> component : components) {
            if (component.getState() == LogicalState.NEW) {
                allocator.allocate(component, plans, false);
            }
        }
    }

    /**
     * Selects bindings for references targeted to remote services for a set of components being deployed by delegating to a BindingSelector.
     *
     * @param components the set of components being deployed
     * @throws DeploymentException if an error occurs during binding selection
     */
    private void selectBinding(Collection<LogicalComponent<?>> components) throws DeploymentException {
        for (LogicalComponent<?> component : components) {
            if (component.getState() == LogicalState.NEW) {
                try {
                    bindingSelector.selectBindings(component);
                } catch (BindingSelectionException e) {
                    throw new DeploymentException(e);
                }
            }
        }
    }

    private Set<Contribution> resolveContributions(List<URI> uris) {
        Set<Contribution> contributions = new LinkedHashSet<Contribution>(uris.size());
        for (URI uri : uris) {
            Contribution contribution = metadataStore.find(uri);
            contributions.add(contribution);
        }
        return contributions;
    }

    /**
     * Returns the list of deployable composites contained in the list of contributions that are configured to run in the current runtime mode
     *
     * @param contributions the contributions containing the deployables
     * @return the list of deployables
     */
    private List<Composite> getDeployables(Set<Contribution> contributions) {
        List<Composite> deployables = new ArrayList<Composite>();
        for (Contribution contribution : contributions) {
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> entry : resource.getResourceElements()) {
                    if (!(entry.getValue() instanceof Composite)) {
                        continue;
                    }
                    @SuppressWarnings({"unchecked"})
                    ResourceElement<QNameSymbol, Composite> element = (ResourceElement<QNameSymbol, Composite>) entry;
                    QName name = element.getSymbol().getKey();
                    Composite composite = element.getValue();
                    for (Deployable deployable : contribution.getManifest().getDeployables()) {
                        if (deployable.getName().equals(name)) {
                            List<RuntimeMode> deployableModes = deployable.getRuntimeModes();
                            RuntimeMode runtimeMode = info.getRuntimeMode();
                            // only add deployables that are set to boot in the current runtime mode
                            if (deployableModes.contains(runtimeMode)) {
                                deployables.add(composite);
                            }
                            break;
                        }
                    }
                }
            }
        }
        return deployables;
    }

    /**
     * Returns a list of deployment plans contained in the list of contributions.
     *
     * @param contributions the contributions plans
     * @return the deployment plans
     */
    private List<DeploymentPlan> getDeploymentPlans(Set<Contribution> contributions) {
        List<DeploymentPlan> plans = new ArrayList<DeploymentPlan>();
        for (Contribution contribution : contributions) {
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> entry : resource.getResourceElements()) {
                    if (!(entry.getValue() instanceof DeploymentPlan)) {
                        continue;
                    }
                    @SuppressWarnings({"unchecked"})
                    ResourceElement<QNameSymbol, DeploymentPlan> element = (ResourceElement<QNameSymbol, DeploymentPlan>) entry;
                    DeploymentPlan plan = element.getValue();
                    plans.add(plan);
                }
            }
        }
        return plans;
    }


}
