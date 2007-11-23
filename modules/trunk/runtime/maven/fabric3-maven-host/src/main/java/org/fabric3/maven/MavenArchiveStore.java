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

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;

import org.fabric3.spi.services.archive.ArchiveStore;
import org.fabric3.spi.services.archive.ArchiveStoreException;
import org.fabric3.spi.services.artifact.Artifact;

/**
 * An archive store that delegates to a set of local and remote Maven repositories.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class MavenArchiveStore implements ArchiveStore {
    private static final String DEFAULT_REPO = "http://repo1.maven.org/maven2/";
    private String id = "extensions";
    private String remoteRepositories = DEFAULT_REPO;
    private MavenHelper helper;

    public String getId() {
        return id;
    }

    @Property(required = false)
    public void setId(String id) {
        this.id = id;
    }

    @Property(required = false)
    public void setRemoteRepositories(String remoteRepositories) {
        this.remoteRepositories = remoteRepositories;
    }

    @Init
    public void init() {
        helper = new MavenHelper(remoteRepositories, true);
        helper.start();
    }

    @Destroy
    public void destroy() {
        helper.stop();
    }

    public URL store(URI uri, InputStream stream) throws ArchiveStoreException {
        return find(uri);
    }

    public URL store(URI contributionUri, URL sourceURL) throws ArchiveStoreException {
        return find(contributionUri);
    }

    public URL find(URI uri) throws ArchiveStoreException {
        // assume uri is in the form 'group id:artifact id: version'
        String[] parsed = uri.toString().split(":");
        Artifact artifact = new Artifact();
        artifact.setGroup(parsed[0]);
        artifact.setName(parsed[1]);
        artifact.setVersion(parsed[2]);
        artifact.setType("jar");
        try {
            if (!helper.resolveTransitively(artifact)) {
                return null;
            }
        } catch (Fabric3DependencyException e) {
            throw new ArchiveStoreException("Error finding archive", uri.toString(), e);
        }
        return artifact.getUrl();
    }

    public void remove(URI uri) {
        throw new UnsupportedOperationException();
    }

    public List<URI> list() {
        throw new UnsupportedOperationException();
    }


}
