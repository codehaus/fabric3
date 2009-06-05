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
package org.fabric3.fabric.instantiator;

import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Resolves promoted services and references by setting the resolved promotion URI of the logical component service or reference that is being
 * promoted.
 *
 * @version $Revision$ $Date$
 */
public interface PromotionResolutionService {

    /**
     * Handles promotion on the specified logical service.
     * <p/>
     * Promoted URIs are of the general form <code>componentId#serviceName</code>, where the service name is optional. If the  promoted URI doesn't
     * contain a fragment for the service name, the promoted component is expected to have exactly one service. If the service fragment is present the
     * promoted component is required to have a service by the name. If the service fragment was not specified, the promoted URI is set to the URI of
     * the promoted service.
     *
     * @param logicalService Logical service whose promotion is handled.
     * @param context        the instantiation context. Recoverable errors and warnings should be reported here.
     */
    void resolve(LogicalService logicalService, InstantiationContext context);

    /**
     * Handles all promotions on the specified logical reference.
     * <p/>
     * Promoted URIs are of the general form <code>componentId#referenceName</code>, where the reference name is optional. If the  promoted URI
     * doesn't contain a fragment for the reference name, the promoted component is expected to have exactly one reference. If the reference fragment
     * is present the promoted component is required to have a reference by the name. If the reference fragment was not specified, the promoted URI is
     * set to the URI of the promoted reference.
     *
     * @param logicalReference Logical reference whose promotion is handled.
     * @param context          the instantiation context. Recoverable errors and warnings should be reported here.
     */
    void resolve(LogicalReference logicalReference, InstantiationContext context);

}
