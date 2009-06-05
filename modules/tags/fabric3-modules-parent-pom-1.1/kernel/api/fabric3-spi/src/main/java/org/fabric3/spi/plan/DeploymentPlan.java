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
package org.fabric3.spi.plan;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 * Represents a Fabric3 deployment plan. Deployment plans are used to map a logical assembly to a physical topology during deployment. For example, a
 * deployment plan may map deployable composites to domain zones.
 *
 * @version $Revision$ $Date$
 */
public class DeploymentPlan implements Serializable {
    private static final long serialVersionUID = 4925927937202340746L;

    private String name;
    private Map<QName, String> deployableMappings = new HashMap<QName, String>();

    /**
     * Constructor.
     *
     * @param name the unique deployment plan name
     */
    public DeploymentPlan(String name) {
        this.name = name;
    }

    /**
     * Gets the deployment plan name.
     *
     * @return the deployment plan name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the deployable composite name to zone mappings.
     *
     * @return the deployable composite name to zone mappings
     */
    public Map<QName, String> getDeployableMappings() {
        return deployableMappings;
    }

    /**
     * Sets a deployable composite name to zone mapping
     *
     * @param name the deployable composite name
     * @param zone the zone name
     */
    public void addDeployableMapping(QName name, String zone) {
        deployableMappings.put(name, zone);
    }
}
