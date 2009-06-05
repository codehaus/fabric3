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
package org.fabric3.activemq.broker;

import java.net.URI;

/**
 * Encapsulates transport connector adapter configuration for a broker.
 *
 * @version $Revision$ $Date$
 */
public class TransportConnectorConfig {
    private URI uri;
    private URI discoveryUri;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public URI getDiscoveryUri() {
        return discoveryUri;
    }

    public void setDiscoveryUri(URI discoveryUri) {
        this.discoveryUri = discoveryUri;
    }
}
