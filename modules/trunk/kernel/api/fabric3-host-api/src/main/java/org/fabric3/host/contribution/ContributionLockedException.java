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
package org.fabric3.host.contribution;

import java.net.URI;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Thrown when an attempt is made to uninstall a contribution that is referenced by deployed components.
 *
 * @version $Revision$ $Date$
 */
public class ContributionLockedException extends ContributionException {
    private static final long serialVersionUID = -5443601943113359365L;
    private URI uri;
    private Set<QName> deployables;

    public ContributionLockedException(String message, URI uri, Set<QName> deployables) {
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
