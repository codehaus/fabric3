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
package org.fabric3.contribution;

import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.services.contribution.ContributionWire;

/**
 * Connects an importing contribution classloader to the classloader of the contribution containing the resolved export as specified by a
 * ContributionWire.
 *
 * @version $Revision$ $Date$
 */
public interface ContributionWireConnector<T extends ContributionWire> {

    /**
     * Connects the importing contribution classloader to the exporting contribution classloader according to the given ContributionWire.
     *
     * @param wire                 the contribution wire
     * @param importingClassLoader the classloader of the importing contribution
     * @param exportingClassLoader the classloader of the exporting contribution
     */
    void connect(T wire, MultiParentClassLoader importingClassLoader, ClassLoader exportingClassLoader);

}
