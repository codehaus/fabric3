/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
    private String mappingPath = "/contribution";

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
        String scheme = uri.getScheme();
        if (scheme == null) {
            // no scheme present, use the default
            scheme = EncodingConstants.DEFAULT_SCHEME;
        }
        String path = mappingPath + "/" + scheme + "/" + uri.getPath();
        return new URI("http", null, address, port, path, null, null);
    }
}
