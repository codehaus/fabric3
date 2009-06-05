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

import java.util.List;
import java.util.Map;

import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Calculates contributions required for a deployment.
 *
 * @version $Revision$ $Date$
 */
public interface ContributionCollator {

    /**
     * Collates contributions for components being deployed or undeployed by zone. That is, the list of components is processed to determine the
     * required set of contributions keyed by zone where the components are to be deployed to or undeployed from.
     *
     * @param components the set of components
     * @param type       the type of generation being performed: incremental deploy, full deploy; or undeploy
     * @return the set of required contributions grouped by zone
     */
    Map<String, List<Contribution>> collateContributions(List<LogicalComponent<?>> components, GenerationType type);
}
