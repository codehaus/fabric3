/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.spi.allocator;

import java.util.List;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.plan.DeploymentPlan;

/**
 * Allocates a component to a service node.
 *
 * @version $Rev$ $Date$
 */
public interface Allocator {

    /**
     * Performs the allocation. Composites are recursed and their children are allocated.
     *
     * @param component the component to allocate
     * @param plans     the deployment plans to use for mapping components to domain zones
     * @param recover   true if the allocator is called while the controller is recovering.
     * @throws AllocationException if an error during allocation occurs
     */
    void allocate(LogicalComponent<?> component, List<DeploymentPlan> plans, boolean recover) throws AllocationException;
}
