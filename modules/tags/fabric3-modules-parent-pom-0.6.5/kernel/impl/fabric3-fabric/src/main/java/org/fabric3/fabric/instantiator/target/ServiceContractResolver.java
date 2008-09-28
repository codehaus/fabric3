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
package org.fabric3.fabric.instantiator.target;

import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Resolves the service contract for services and references. Promoted services and references often do not specify a service contract explicitly,
 * instead using a contract defined further down in the promotion hierarchy. In these cases, the service contract is often inferred from the
 * implementation (e.g. a Java class) or explicitly declared within the component definition in a composite file.
 *
 * @version $Revision$ $Date$
 */
public interface ServiceContractResolver {

    /**
     * Returns the contract for a service.
     *
     * @param service the service to determine the service contract for.
     * @return the contract or null if none is found
     */
    ServiceContract<?> determineContract(LogicalService service);

    /**
     * Returns the contract for a reference.
     *
     * @param reference the reference to determine the service contract for.
     * @return the contract or null if none is found
     */
    ServiceContract<?> determineContract(LogicalReference reference);

}
