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
package org.fabric3.runtime.webapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.fabric3.host.contribution.ContributionSource;

/**
 * Used to install the current webapp as a contribution.w
 *
 * @version $Rev$ $Date$
 */
public class WarContributionSource implements ContributionSource {

    private static final String CONTENT_TYPE = "application/vnd.fabric3.war";

    private URI contributionUri;
    private URL url;
    private byte[] checksum;
    private long timestamp;

    public WarContributionSource(URI contributionUri) throws MalformedURLException {
        this.contributionUri = contributionUri;
        this.url = new File("/").toURI().toURL();
        checksum = new byte[0];
        timestamp = System.currentTimeMillis();
    }

    public boolean persist() {
        return false;
    }

    public URI getUri() {
        return contributionUri;
    }

    public InputStream getSource() throws IOException {
        return null;
    }

    public URL getLocation() {
        return url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public String getContentType() {
        return CONTENT_TYPE;
    }
}
