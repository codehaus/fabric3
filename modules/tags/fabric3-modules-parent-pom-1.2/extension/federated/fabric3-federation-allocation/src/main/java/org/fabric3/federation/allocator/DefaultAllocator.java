/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.federation.allocator;

import java.util.List;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

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


    public DefaultAllocator(@Reference DomainManager domainManager) {
        this.domainManager = domainManager;
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
    }

}
