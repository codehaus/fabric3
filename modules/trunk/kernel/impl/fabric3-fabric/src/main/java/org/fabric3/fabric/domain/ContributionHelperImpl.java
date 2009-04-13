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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.StoreException;
import org.fabric3.host.domain.CompositeAlreadyDeployedException;
import org.fabric3.host.domain.DeployableNotFoundException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.plan.DeploymentPlan;

/**
 * Utilities used by the Domain for introspecting information from a contribution.
 *
 * @version $Revision$ $Date$
 */
public class ContributionHelperImpl implements ContributionHelper {
    private static final String PLAN_NAMESPACE = "urn:fabric3.org:extension:plan";
    private MetaDataStore metadataStore;

    public ContributionHelperImpl(@Reference MetaDataStore metadataStore) {
        this.metadataStore = metadataStore;
    }

    public List<Composite> getDeployables(Set<Contribution> contributions, RuntimeMode runtimeMode) {
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

    public List<DeploymentPlan> getDeploymentPlans(Set<Contribution> contributions) {
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

    public Composite resolveComposite(QName deployable) throws DeploymentException {
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


    public DeploymentPlan resolvePlan(String plan) throws DeploymentException {
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

    public Set<Contribution> resolveContributions(List<URI> uris) {
        Set<Contribution> contributions = new LinkedHashSet<Contribution>(uris.size());
        for (URI uri : uris) {
            Contribution contribution = metadataStore.find(uri);
            contributions.add(contribution);
        }
        return contributions;
    }

    public void lock(Set<Contribution> contributions) throws CompositeAlreadyDeployedException {
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
    }

    public void releaseLocks(Set<Contribution> contributions) {
        for (Contribution contribution : contributions) {
            for (Deployable deployable : contribution.getManifest().getDeployables()) {
                QName name = deployable.getName();
                if (contribution.getLockOwners().contains(name)) {
                    contribution.releaseLock(name);
                }
            }
        }
    }


}
