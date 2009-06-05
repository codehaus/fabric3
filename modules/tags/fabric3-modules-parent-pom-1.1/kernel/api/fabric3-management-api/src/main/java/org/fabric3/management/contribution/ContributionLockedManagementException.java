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
package org.fabric3.management.contribution;

import java.net.URI;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * @version $Revision$ $Date$
 */
public class ContributionLockedManagementException extends ContributionUninstallException {
    private static final long serialVersionUID = 5508297045249783700L;
    private URI uri;
    private Set<QName> deployables;

    /**
     * Constructor.
     *
     * @param message     the error message
     * @param uri         the contribution
     * @param deployables the deployed composites that are using to the contribution
     */
    public ContributionLockedManagementException(String message, URI uri, Set<QName> deployables) {
        super(message);
        this.uri = uri;
        this.deployables = deployables;
    }

    public URI getUri() {
        return uri;
    }

    public Set<QName> getDeployables() {
        return deployables;
    }
}