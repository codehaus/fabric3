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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * A contribution artifact that is on a filesystem
 *
 * @version $Rev$ $Date$
 */
public class FileContributionSource implements ContributionSource {
    private URI uri;
    private URL location;
    private long timestamp;
    private byte[] checksum;
    private String contentType;


    public FileContributionSource(URL location, long timestamp, byte[] checksum) {
        this(null, location, timestamp, checksum);
    }

    public FileContributionSource(URI uri, URL location, long timestamp, byte[] checksum) {
        this(uri, location, timestamp, checksum, null);
    }

    public FileContributionSource(URI uri, URL location, long timestamp, byte[] checksum, String contentType) {
        this.uri = uri;
        this.location = location;
        this.timestamp = timestamp;
        this.checksum = checksum;
        this.contentType = contentType;
    }

    public boolean persist() {
        return false;
    }

    public URI getUri() {
        return uri;
    }

    public InputStream getSource() throws IOException {
        return location.openStream();
    }

    public URL getLocation() {
        return location;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public String getContentType() {
        return contentType;
    }
}


