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
import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.services.contribution.ContributionWireInstantiator;
import org.fabric3.spi.services.contribution.ContributionWire;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class ContributionWireInstantiatorRegistryImpl implements ContributionWireInstantiatorRegistry {
    private Map<Class<? extends Import>, ContributionWireInstantiator<?, ?, ?>> instantiators =
            new HashMap<Class<? extends Import>, ContributionWireInstantiator<?, ?, ?>>();

    @Reference
    public void setInstantiators(Map<Class<? extends Import>, ContributionWireInstantiator<?, ?, ?>> instantiors) {
        this.instantiators = instantiors;
    }


    public <I extends Import, E extends Export> ContributionWire<I, E> instantiate(I imprt, E export, URI importUri, URI exportUri) {
        ContributionWireInstantiator instantiator = instantiators.get(imprt.getClass());
        if (instantiator == null) {
            throw new AssertionError("Insantiator not configured: " + imprt.getClass());
        }
        // cast is safe
        //noinspection unchecked
        return instantiator.instantiate(imprt, export, importUri, exportUri);

    }

}
