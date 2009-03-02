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
package org.fabric3.spi.contribution;

import java.io.Serializable;
import java.net.URI;

/**
 * Represents a connection between a contribution import and a resolved contribution export. ContributionWire subtypes define specific semantics for
 * artifact visibility. For example, a Java-based wire may restrict visibility to a set of packages while a QName-based wire may restrict visibility
 * to a set of artifacts of a specified QName.
 *
 * @version $Revision$ $Date$
 */
public interface ContributionWire<I extends Import, E extends Export> extends Serializable {

    /**
     * Returns the import for this wire.
     *
     * @return the import for this wire
     */
    I getImport();

    /**
     * Returns the export this wire is mapped to.
     *
     * @return the export this wire is mapped to
     */
    E getExport();

    /**
     * Returns the importing contribution URI.
     *
     * @return the importing contribution URI
     */
    URI getImportContributionUri();

    /**
     * Returns the resolved exporting contribution URI.
     *
     * @return the resolved exporting contribution URI
     */
    URI getExportContributionUri();

    /**
     * Returns true if the wire resolves the resource.
     *
     * @param resource the Symbol representing the resource to resolve
     * @return true if the wire resolves the resource
     */
    boolean resolves(Symbol resource);

}
