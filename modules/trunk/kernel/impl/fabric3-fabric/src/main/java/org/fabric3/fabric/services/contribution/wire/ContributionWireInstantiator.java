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
 * Implementations instantiate a ContributionWire between a contribution import and a resolved export from another contribution.
 *
 * @version $Revision$ $Date$
 */
public interface ContributionWireInstantiator<I extends Import, E extends Export, CW extends ContributionWire<I, E>> {

    /**
     * Instantiates the wire.
     *
     * @param imprt     the import
     * @param export    the resolved export
     * @param importUri the URI of the contribution containing the import
     * @param exportUri the URI of the contribution containing the export
     * @return the ContributionWire
     */
    CW instantiate(I imprt, E export, URI importUri, URI exportUri);

}
