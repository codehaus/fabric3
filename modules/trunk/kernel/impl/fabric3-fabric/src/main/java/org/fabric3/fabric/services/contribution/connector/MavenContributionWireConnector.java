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
package org.fabric3.fabric.services.contribution.connector;

import org.fabric3.fabric.services.contribution.ContributionWireConnector;
import org.fabric3.fabric.services.contribution.wire.MavenContributionWire;
import org.fabric3.spi.classloader.MultiParentClassLoader;

/**
 * Connects an importing contribution classloader to an exporting contribution classloader, with no visibility constraints.
 * <p/>
 * Note: This could be be extended to restrict visibility according to Maven scopes.
 *
 * @version $Revision$ $Date$
 */
public class MavenContributionWireConnector implements ContributionWireConnector<MavenContributionWire> {

    public void connect(MavenContributionWire wire, MultiParentClassLoader importingClassLoader, ClassLoader exportingClassLoader) {
        if (!importingClassLoader.getParents().contains(exportingClassLoader)) {
            importingClassLoader.addParent(exportingClassLoader);
        }
    }

}