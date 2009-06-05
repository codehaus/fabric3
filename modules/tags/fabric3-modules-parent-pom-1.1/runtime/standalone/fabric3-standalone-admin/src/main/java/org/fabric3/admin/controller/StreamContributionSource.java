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
 * A ContributionSource that wraps an underlying input stream to avoid closing it. This implementation is used to handle input streams that contain
 * multiple contribution archives.
 *
 * @version $Revision$ $Date$
 */
public class StreamContributionSource implements ContributionSource {
    private URI uri;
    private StreamWrapper wrapped;

    public StreamContributionSource(URI uri, InputStream stream) {
        this.uri = uri;
        this.wrapped = new StreamWrapper(stream);
    }

    public boolean persist() {
        return true;
    }

    public URI getUri() {
        return uri;
    }

    public InputStream getSource() throws IOException {
        return wrapped;
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

    private class StreamWrapper extends InputStream {
        private InputStream wrapped;

        private StreamWrapper(InputStream wrapped) {
            this.wrapped = wrapped;
        }

        public int read() throws IOException {
            return wrapped.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return wrapped.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return wrapped.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return wrapped.skip(n);
        }

        @Override
        public int available() throws IOException {
            return wrapped.available();
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }

        @Override
        public void mark(int readlimit) {
            wrapped.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            wrapped.reset();
        }

        @Override
        public boolean markSupported() {
            return wrapped.markSupported();
        }

        @Override
        public int hashCode() {
            return wrapped.hashCode();
        }

        @Override
        public String toString() {
            return wrapped.toString();
        }

    }
}