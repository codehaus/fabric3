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
package org.fabric3.spi.policy;

import java.util.List;

import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;


/**
 * Interface for the policy resolver.
 *
 * @version $Revision$ $Date$
 */
public interface PolicyResolver {

    /**
     * Attaches all active PolicySets (i.e. those that use external attachment) to the component hierarchy.
     *
     * @param component   the top-most component to evaluate external attachments against
     * @param incremental true if the attachment is performed as part of an incremental deployment
     * @throws PolicyResolutionException if an error occurs evaluating the policies
     */
    void attachPolicies(LogicalComponent<?> component, boolean incremental) throws PolicyResolutionException;

    /**
     * Attaches PolicySets (i.e. those that use external attachment) to the component hierarchy.
     *
     * @param policySets  the policy sets to attach
     * @param component   the top-most component to evaluate external attachments against
     * @param incremental true if the attachment is performed as part of an incremental deployment
     * @throws PolicyResolutionException if an error occurs evaluating the policies
     */
    void attachPolicies(List<PolicySet> policySets, LogicalComponent<?> component, boolean incremental) throws PolicyResolutionException;

    /**
     * Detaches PolicySets (i.e. those that use external attachment) to the component hierarchy.
     *
     * @param policySets the policy sets to detach
     * @param component  the top-most component to evaluate external attachments against
     * @throws PolicyResolutionException if an error occurs evaluating the policies
     */
    void detachPolicies(List<PolicySet> policySets, LogicalComponent<?> component) throws PolicyResolutionException;

    /**
     * Resolves all the interaction and implementation intents for the operations on wire.
     *
     * @param operations    the operations to resolve policies for
     * @param sourceBinding Source binding.
     * @param targetBinding Target binding.
     * @param source        Source component.
     * @param target        Target component.
     * @return Policy resolution result.
     * @throws PolicyResolutionException If unable to resolve any policies.
     */
    PolicyResult resolvePolicies(List<LogicalOperation> operations,
                                 LogicalBinding<?> sourceBinding,
                                 LogicalBinding<?> targetBinding,
                                 LogicalComponent<?> source,
                                 LogicalComponent<?> target) throws PolicyResolutionException;

}
