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
package org.fabric3.maven;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.fabric3.host.contribution.ContributionSource;

/**
 * An extension contribution
 *
 * @version $Rev$ $Date$
 */
public class MavenExtensionContributionSource implements ContributionSource {
    public static final String MAVEN_CONTENT_TYPE = "application/vnd.fabric3.maven";
    private static final byte[] EMPTY_CHECKSUM = new byte[0];
    private byte[] contribution;

    public MavenExtensionContributionSource(String[] extensions) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int i = 0;
        int last = extensions.length - 1;
        for (String contribution : extensions) {
            stream.write(contribution.getBytes());
            if (i != last) {
                stream.write(";".getBytes());
            }
            ++i;
        }
        contribution = stream.toByteArray();
    }

    public boolean isLocal() {
        return true;
    }

    public URI getUri() {
        return null;
    }

    public InputStream getSource() throws IOException {
        return new ByteArrayInputStream(contribution);
    }

    public URL getLocation() {
        return null;
    }

    public String getContentType() throws IOException {
        return MAVEN_CONTENT_TYPE;
    }

    public long getTimestamp() {
        return -1;
    }

    public byte[] getChecksum() {
        // checksum not needed since Maven contributions are not persisted
        return EMPTY_CHECKSUM;
    }

}
