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
package org.fabric3.admin.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.fabric3.host.contribution.ContributionSource;

/**
 * @version $Revision$ $Date$
 */
public class RemoteContributionSource implements ContributionSource {
    private URI uri;
    private InputStream stream;

    public RemoteContributionSource(URI uri, InputStream stream) {
        this.uri = uri;
        this.stream = stream;
    }

    public boolean persist() {
        return true;
    }

    public URI getUri() {
        return uri;
    }

    public InputStream getSource() throws IOException {
        return stream;
    }

    public URL getLocation() {
        return null;
    }

    public long getTimestamp() {
        return 0;
    }

    public byte[] getChecksum() {
        return new byte[0];
    }

    public String getContentType() {
        return null;
    }
}