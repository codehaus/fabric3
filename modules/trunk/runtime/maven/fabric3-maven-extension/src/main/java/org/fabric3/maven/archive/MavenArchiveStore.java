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
package org.fabric3.maven.archive;

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

/**
 * An archive store that delegates to a set of local and remote Maven repositories.
 *
 * @version $Rev: 5976 $ $Date: 2008-11-16 16:10:37 -0800 (Sun, 16 Nov 2008) $
 */
@EagerInit
public class MavenArchiveStore implements ArchiveStore {
    private static final String DEFAULT_REPO = "http://repo1.maven.org/maven2/";
    private String remoteRepositories = DEFAULT_REPO;
    private MavenHelper helper;

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

    public boolean exists(URI uri) {
        // always return false
        return false;
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
            String id = uri.toString();
            throw new ArchiveStoreException("Error finding archive: " + id, id, e);
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
