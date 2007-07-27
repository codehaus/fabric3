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

    public FileContributionSource(URL location, long timestamp, byte[] checksum) {
        this(null, location, timestamp, checksum);
    }

    public FileContributionSource(URI uri, URL location, long timestamp, byte[] checksum) {
        this.uri = uri;
        this.location = location;
        this.timestamp = timestamp;
        this.checksum = checksum;
    }

    public boolean isLocal() {
        return true;
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
}
