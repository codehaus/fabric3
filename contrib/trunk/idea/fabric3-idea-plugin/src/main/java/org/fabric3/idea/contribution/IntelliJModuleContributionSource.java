/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.idea.contribution;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.fabric3.host.contribution.ContributionSource;

/**
 * Encapsualtes an IntelliJ module contribution location.
 *
 * @version $Rev$ $Date$
 */
public class IntelliJModuleContributionSource implements ContributionSource {
    private URI uri;
    private URL url;
    private long timestamp;
    private byte[] checksum;

    public IntelliJModuleContributionSource(URI uri) throws MalformedURLException {
        this.uri = uri;
        url = new URL("file", "", -1, "", new IntelliJModuleStreamHandler());
        checksum = new byte[0];
        timestamp = System.currentTimeMillis();
    }

    public boolean persist() {
        return false;
    }

    public URI getUri() {
        return uri;
    }

    public InputStream getSource() throws IOException {
        return url.openStream();
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
}
