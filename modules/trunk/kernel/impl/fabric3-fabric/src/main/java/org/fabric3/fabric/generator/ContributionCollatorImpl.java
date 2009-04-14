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
package org.fabric3.fabric.generator;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.Names;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 * @version $Revision$ $Date$
 */
public class ContributionCollatorImpl implements ContributionCollator {
    private MetaDataStore store;

    public ContributionCollatorImpl(@Reference MetaDataStore store) {
        this.store = store;
    }

    public Map<String, List<Contribution>> collateContributions(List<LogicalComponent<?>> components, GenerationType type) {
        // collate all contributions that must be provisioned as part of the change set
        Map<String, List<Contribution>> contributionsPerZone = new HashMap<String, List<Contribution>>();
        for (LogicalComponent<?> component : components) {
            if (type != GenerationType.FULL) {
                if (GenerationType.INCREMENTAL == type && LogicalState.NEW != component.getState()) {
                    continue;
                } else if (GenerationType.UNDEPLOY == type && LogicalState.MARKED != component.getState()) {
                    continue;
                }
            }
            URI contributionUri = component.getDefinition().getContributionUri();
            String zone = component.getZone();
            List<Contribution> contributions = contributionsPerZone.get(zone);
            if (contributions == null) {
                contributions = new ArrayList<Contribution>();
                contributionsPerZone.put(zone, contributions);
            }
            Contribution contribution = store.find(contributionUri);
            // imported contributions must also be provisioned
            List<ContributionWire<?, ?>> contributionWires = contribution.getWires();
            for (ContributionWire<?, ?> wire : contributionWires) {
                URI importedUri = wire.getExportContributionUri();
                Contribution imported = store.find(importedUri);
                if (!contributions.contains(imported) && !Names.HOST_CONTRIBUTION.equals(importedUri)) {
                    contributions.add(imported);
                }
            }
            contributions.add(contribution);
        }
        return contributionsPerZone;
    }

}
