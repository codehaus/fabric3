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

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.spi.allocator.AllocationException;
import org.fabric3.spi.allocator.Allocator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.topology.DomainManager;
import org.fabric3.spi.topology.Zone;

/**
 * Allocator that selectes the default (first available) zone.
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

    public void allocate(LogicalComponent<?> component, boolean recover) throws AllocationException {
        if (component.getZone() == null) {
            if (component instanceof LogicalCompositeComponent) {
                LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
                for (LogicalComponent<?> child : composite.getComponents()) {
                    allocate(child, recover);
                }
            }
            selectZone(component);
        }
    }

    private void selectZone(LogicalComponent<?> component) throws AllocationException {
        List<Zone> zones = domainManager.getZones();
        if (zones.isEmpty()) {
            throw new NoZonesAvailableException("No zones are available for deployment in domain");
        }
        // for now, pick the first one
        Zone zone = zones.get(0);
        String zoneName = zone.getName();
        component.setZone(zoneName);
        monitor.selected(zoneName);
    }
}
