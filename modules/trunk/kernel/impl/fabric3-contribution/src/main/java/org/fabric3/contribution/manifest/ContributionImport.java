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
package org.fabric3.contribution.manifest;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.spi.Namespaces;
import org.fabric3.spi.services.contribution.Import;

/**
 * Imports a contribution by URI. All contents of the resolved exporting contribution are visible.
 *
 * @version $Revision$ $Date$
 */
public class ContributionImport implements Import {
    private static final long serialVersionUID = 5947082714758125178L;
    private static final QName TYPE = new QName(Namespaces.CORE, "contributionImport");
    private URI location;

    public ContributionImport(URI contibutionId) {
        location = contibutionId;
    }

    public QName getType() {
        return TYPE;
    }

    public URI getLocation() {
        return location;
    }

    public void setLocation(URI location) {
        this.location = location;
    }
}
