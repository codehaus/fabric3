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
package org.fabric3.spi.generator;

import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.model.physical.PhysicalClassLoaderWireDefinition;

/**
 * Generates a PhysicalClassLoaderWireDefinition from a ContributionWire. The physical definition is used to build a classloader network based on
 * contribution wires.
 *
 * @version $Revision$ $Date$
 */
public interface ClassLoaderWireGenerator<T extends ContributionWire> {

    /**
     * Generate the physical definition.
     *
     * @param wire the contribution wire to use as input
     * @return the physical definition
     */
    PhysicalClassLoaderWireDefinition generate(T wire);
}
