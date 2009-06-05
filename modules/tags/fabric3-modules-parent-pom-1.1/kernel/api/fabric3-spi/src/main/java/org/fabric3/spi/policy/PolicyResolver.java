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
