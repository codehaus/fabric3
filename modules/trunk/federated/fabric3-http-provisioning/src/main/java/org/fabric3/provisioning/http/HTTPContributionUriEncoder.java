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
package org.fabric3.provisioning.http;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.services.contribution.ContributionUriEncoder;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Encodes a contribution URI so it can be dereferenced in a domain via HTTP. The encoding maps from the contribution URI to an HTTP-based URI.
 *
 * @version $Revision$ $Date$
 */
public class HTTPContributionUriEncoder implements ContributionUriEncoder {
    private ServletHost host;
    private MetaDataStore store;
    private String address;
    private int port;
    private String mappingPath = "/repository";

    public HTTPContributionUriEncoder(@Reference ServletHost host, @Reference MetaDataStore store) {
        this.host = host;
        this.store = store;
    }

    @Property
    public void setMappingPath(String path) {
        mappingPath = path;
    }

    @Property
    public void setHttpPort(String port) {
        this.port = Integer.parseInt(port);
    }

    @Property
    public void setAddress(String address) {
        this.address = address;
    }

    @Init
    public void init() throws UnknownHostException {
        if (address == null) {
            address = InetAddress.getLocalHost().getHostAddress();
        }
        host.registerMapping(mappingPath + "/*", new ArchiveResolverServlet(store));
    }

    public URI encode(URI uri) throws URISyntaxException {
        String path = mappingPath + "/" + uri.getPath();
        return new URI("http", null, address, port, path, null, null);
    }
}
