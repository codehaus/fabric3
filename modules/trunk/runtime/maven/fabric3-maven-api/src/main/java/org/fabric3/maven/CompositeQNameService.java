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
package org.fabric3.maven;

import java.net.URL;
import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.host.Names;

/**
 * Returns the QName of a composite.
 *
 * @version $Revision$ $Date$
 */
public interface CompositeQNameService {

    URI SERVICE_URI = URI.create(Names.RUNTIME_NAME + "/CompositeQNameService");

    /**
     * Returns the composite qname.
     *
     * @param uri the URI of the contribution to search
     * @param url the URL of the composite
     * @return the QName.
     * @throws InvalidResourceException      if the resource pointed to the URL is invalid or missing.
     * @throws ContributionNotFoundException if the contribution does not exist
     */
    QName getQName(URI uri, URL url) throws ContributionNotFoundException, InvalidResourceException;
}
