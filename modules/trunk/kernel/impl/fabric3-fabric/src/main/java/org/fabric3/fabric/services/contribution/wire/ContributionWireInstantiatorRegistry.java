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
package org.fabric3.fabric.services.contribution.wire;

import java.net.URI;

import org.fabric3.spi.services.contribution.ContributionWire;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;

/**
 * A registry used to dispatch to ContributionWireInstantiators.
 * <p/>
 * This is is required since the kernel does not support reinjection of multiplicity references on Singleton components (it does, however, support
 * reinjection of non-multiplicity references, which is done with this service).
 *
 * @version $Revision$ $Date$
 */
public interface ContributionWireInstantiatorRegistry {

    /**
     * Dispatches to the instantiator to create the wire.
     *
     * @param imprt     the wire's import
     * @param export    the wire's export
     * @param importUri the importing contribution URI
     * @param exportUri the exporting contribution URI
     * @param <I>       the import type
     * @param <E>       the export type
     * @return the ContributionWire
     */
    <I extends Import, E extends Export> ContributionWire<I, E> instantiate(I imprt, E export, URI importUri, URI exportUri);

}
