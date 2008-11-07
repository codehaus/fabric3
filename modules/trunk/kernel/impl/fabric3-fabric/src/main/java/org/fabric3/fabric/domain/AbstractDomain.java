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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.xml.namespace.QName;

import static org.osoa.sca.Constants.SCA_NS;

import org.fabric3.fabric.binding.BindingSelector;
import org.fabric3.fabric.collector.Collector;
import org.fabric3.fabric.generator.PhysicalModelGenerator;
import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.loader.plan.DeploymentPlanConstants;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Include;
import org.fabric3.spi.allocator.AllocationException;
import org.fabric3.spi.allocator.Allocator;
import org.fabric3.spi.binding.BindingSelectionException;
import org.fabric3.spi.domain.Domain;
import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.CopyUtil;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.plan.DeploymentPlan;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.MetaDataStoreException;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.lcm.LogicalComponentManager;
import org.fabric3.spi.services.lcm.StoreException;
import org.fabric3.spi.services.routing.RoutingException;
import org.fabric3.spi.services.routing.RoutingService;

/**
 * Base class for a domain.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractDomain implements Domain {
    public static final QName COMPOSITE = new QName(SCA_NS, "composite");

    private final MetaDataStore metadataStore;
    private final LogicalComponentManager logicalComponentManager;
    protected LogicalModelInstantiator logicalModelInstantiator;
    protected BindingSelector bindingSelector;
    protected RoutingService routingService;
    protected PhysicalModelGenerator physicalModelGenerator;
    protected Collector collector;

    // The service for allocating to remote zones. Domain subtypes may optionally inject this service if they support distributed domains.
    protected Allocator allocator;

    /**
     * Constructor.
     *
     * @param metadataStore            the store for resolving contribution artifacts
     * @param logicalComponentManager  the manager for logical components
     * @param physicalModelGenerator   the physical model generator
     * @param logicalModelInstantiator the logical model instantiator
     * @param bindingSelector          the selector for binding.sca
     * @param routingService           the service for routing deployment commands
     * @param collector                the collector for undeploying componentsco
     */
    public AbstractDomain(MetaDataStore metadataStore,
                          LogicalComponentManager logicalComponentManager,
                          PhysicalModelGenerator physicalModelGenerator,
                          LogicalModelInstantiator logicalModelInstantiator,
                          BindingSelector bindingSelector,
                          RoutingService routingService,
                          Collector collector) {
        this.metadataStore = metadataStore;
        this.physicalModelGenerator = physicalModelGenerator;
        this.logicalModelInstantiator = logicalModelInstantiator;
        this.logicalComponentManager = logicalComponentManager;
        this.bindingSelector = bindingSelector;
        this.routingService = routingService;
        this.collector = collector;
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
            include(wrapper, null, transactional);
        } else {
            DeploymentPlan deploymentPlan = resolvePlan(plan);
            include(wrapper, deploymentPlan, transactional);
        }

    }

    public void include(Composite composite) throws DeploymentException {
        include(composite, null, false);
    }

    public void include(Composite composite, DeploymentPlan plan, boolean transactional) throws DeploymentException {
        List<DeploymentPlan> plans = new ArrayList<DeploymentPlan>();
        plans.add(plan);
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        if (transactional) {
            domain = CopyUtil.copy(domain);
        }
        LogicalChange change = logicalModelInstantiator.include(domain, composite);
        if (change.hasErrors()) {
            throw new AssemblyException(change.getErrors(), change.getWarnings());
        } else if (change.hasWarnings()) {
            // TOOD log warnings
        }
        allocateAndDeploy(domain, plans, change);

    }

    public void include(List<Composite> composites, List<DeploymentPlan> plans, boolean transactional) throws DeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        if (transactional) {
            domain = CopyUtil.copy(domain);
        }
        LogicalChange change = logicalModelInstantiator.include(domain, composites);
        if (change.hasErrors()) {
            throw new AssemblyException(change.getErrors(), change.getWarnings());
        } else if (change.hasWarnings()) {
            // TOOD log warnings
        }

        allocateAndDeploy(domain, plans, change);
    }

    public void undeploy(QName deployable) throws DeploymentException {
        undeploy(deployable, false);
    }

    public void undeploy(QName deployable, boolean transactional) throws DeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        if (transactional) {
            domain = CopyUtil.copy(domain);
        }
        LogicalChange change = collector.mark(deployable, domain);
        if (change.hasErrors()) {
            throw new AssemblyException(change.getErrors(), change.getWarnings());
        } else if (change.hasWarnings()) {
            // TOOD log warnings
        }
        try {
            CommandMap commandMap = physicalModelGenerator.generate(domain.getComponents());
            String id = UUID.randomUUID().toString();
            routingService.route(id, commandMap);
        } catch (GenerationException e) {
            throw new DeploymentException("Error undeploying: " + deployable, e);
        } catch (RoutingException e) {
            throw new DeploymentException("Error undeploying: " + deployable, e);

        }
        try {
            // TODO this should happen after nodes have deployed the components and wires
            logicalComponentManager.replaceRootComponent(domain);
        } catch (StoreException e) {
            throw new DeploymentException("Error applying undeployment: " + deployable, e);
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
            QName planName = new QName(DeploymentPlanConstants.PLAN_NAMESPACE, plan);
            element = metadataStore.resolve(new QNameSymbol(planName));
        } catch (MetaDataStoreException e) {
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
        } catch (MetaDataStoreException e) {
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
     * @param change the logical change
     * @throws DeploymentException if an error is encountered during deployment
     */
    private void allocateAndDeploy(LogicalCompositeComponent domain, List<DeploymentPlan> plans, LogicalChange change) throws DeploymentException {
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
            CommandMap commandMap = physicalModelGenerator.generate(components);
            String id = UUID.randomUUID().toString();
            routingService.route(id, commandMap);
        } catch (GenerationException e) {
            throw new DeploymentException("Error deploying components", e);
        } catch (RoutingException e) {
            throw new DeploymentException("Error deploying components", e);
        }

        try {
            // TODO this should happen after nodes have deployed the components and wires
            markAsProvisioned(change);
            logicalComponentManager.replaceRootComponent(domain);
        } catch (StoreException e) {
            throw new DeploymentException("Error applying deployment", e);
        }
    }

    /**
     * Marks all components, wires, and bindings that are part of a change as provisioned.
     *
     * @param change the logical change
     */
    private void markAsProvisioned(LogicalChange change) {
        for (LogicalComponent<?> component : change.getAddedComponents()) {
            component.setState(LogicalState.PROVISIONED);
        }
        for (LogicalReference reference : change.getAddedReferences()) {
            for (LogicalBinding<?> binding : reference.getBindings()) {
                binding.setState(LogicalState.PROVISIONED);
            }
            for (LogicalBinding<?> binding : reference.getCallbackBindings()) {
                binding.setState(LogicalState.PROVISIONED);
            }
        }
        for (LogicalWire wire : change.getAddedWires()) {
            wire.setState(LogicalState.PROVISIONED);
        }

        for (LogicalService service : change.getAddedServices()) {
            for (LogicalBinding<?> binding : service.getBindings()) {
                binding.setState(LogicalState.PROVISIONED);
            }
            for (LogicalBinding<?> binding : service.getCallbackBindings()) {
                binding.setState(LogicalState.PROVISIONED);
            }
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

}
