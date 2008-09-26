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
package org.fabric3.spi.services.contribution;

import java.io.Serializable;
import java.net.URI;
import javax.xml.namespace.QName;

/**
 * A contribution import
 *
 * @version $Rev$ $Date$
 */
public interface Import extends Serializable {

    /**
     * The QName uniquely identiying the import type.
     *
     * @return the QName uniquely identiying the import type
     */
    QName getType();

    /**
     * A URI representing the import artifact location.
     *
     * @return a URI representing the import artifact location
     */
    URI getLocation();

    /**
     * Sets the URI representing the import artifact location.
     *
     * @param location the URI representing the import artifact location
     */
    void setLocation(URI location);

}
