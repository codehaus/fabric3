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
package org.fabric3.federation.allocator;

import java.util.List;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.spi.allocator.AllocationException;
import org.fabric3.spi.allocator.Allocator;
import org.fabric3.spi.allocator.NoZonesAvailableException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.plan.DeploymentPlan;
import org.fabric3.spi.topology.DomainManager;
import org.fabric3.spi.topology.Zone;

/**
 * Allocator that selectes zones for a collection of components using a deployment plan mappings.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class DefaultAllocator implements Allocator {
    private DomainManager domainManager;
    private AllocatorMonitor monitor;


    public DefaultAllocator(@Reference DomainManager domainManager, @Monitor AllocatorMonitor monitor) {
        this.domainManager = domainManager;
        this.monitor = monitor;
    }

    public void allocate(LogicalComponent<?> component, List<DeploymentPlan> plans, boolean recover) throws AllocationException {
        List<Zone> zones = domainManager.getZones();
        if (zones.isEmpty()) {
            throw new NoZonesAvailableException("No zones are available for deployment in domain");
        }
        if (component.getZone() == null) {
            if (component instanceof LogicalCompositeComponent) {
                LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
                for (LogicalComponent<?> child : composite.getComponents()) {
                    allocate(child, plans, recover);
                }
            }
            selectZone(component, plans, zones);
        }
    }

    /**
     * Maps a component to a zone based on a collection of deployment plans.
     *
     * @param component the component to map
     * @param plans     the deployment plans to use for mapping
     * @param zones     the set of active zones to map to
     * @throws AllocationException if an error occurs mapping
     */
    private void selectZone(LogicalComponent<?> component, List<DeploymentPlan> plans, List<Zone> zones) throws AllocationException {
        QName deployable = component.getDeployable();
        if (deployable == null) {
            // programming error
            throw new AssertionError("Deployable not found for " + component.getUri());
        }
        String zoneName = null;
        for (DeploymentPlan plan : plans) {
            zoneName = plan.getDeployableMappings().get(deployable);
            if (zoneName != null) {
                break;
            }
        }
        if (zoneName == null) {
            throw new DeployableMappingNotFoundException("Zone mapping not found for deployable: " + deployable);
        }
        if (!zones.contains(new Zone(zoneName))) {
            throw new ZoneNotFoundException("Zone not found: " + zoneName);
        }
        component.setZone(zoneName);
        monitor.selected(zoneName);
    }

}
