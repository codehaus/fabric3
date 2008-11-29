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
package org.fabric3.fabric.services.contribution.manifest;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.spi.Namespaces;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;

/**
 * Exports the entire package contents of a contribution. This export type is used for API and SPI contributions where all contents are visible to
 * importing contributions.
 *
 * @version $Revision$ $Date$
 */
public class ContributionExport implements Export {
    private static final long serialVersionUID = -2400233923134603994L;
    private static final QName TYPE = new QName(Namespaces.CORE, "contributionImport");
    private URI location;

    public ContributionExport(URI contibutionId) {
        location = contibutionId;
    }

    public QName getType() {
        return TYPE;
    }

    public URI getLocation() {
        return location;
    }

    public int match(Import imprt) {
        if (imprt instanceof ContributionImport && location.equals(imprt.getLocation())) {
            return EXACT_MATCH;
        } else {
            return NO_MATCH;
        }
    }

}