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
package org.fabric3.runtime.standalone;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.fabric3.host.contribution.ContributionSource;

/**
 * ContributionSource for a directory that serves as a synthetic composite. For example, a datasource directory that contains JDBC drivers.
 *
 * @version $Revision$ $Date$
 */
public class SyntheticContributionSource implements ContributionSource {
    private static final String CONTENT_TYPE = "application/vnd.fabric3.synthetic";
    private URI uri;
    private URL location;

    public SyntheticContributionSource(URI uri, URL location) {
        this.uri = uri;
        this.location = location;
    }

    public String getContentType() {
        return CONTENT_TYPE;
    }

    public boolean persist() {
        return false;
    }

    public URI getUri() {
        return uri;
    }

    public URL getLocation() {
        return location;
    }

    public InputStream getSource() throws IOException {
        throw new UnsupportedOperationException();
    }

    public long getTimestamp() {
        return 0;
    }

    public byte[] getChecksum() {
        return new byte[0];
    }

}
