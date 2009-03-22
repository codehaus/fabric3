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
package org.fabric3.fabric.services.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.repository.Repository;
import org.fabric3.spi.services.repository.RepositoryException;
import org.fabric3.util.io.FileHelper;

/**
 * The default implementation of a Repository that persists artifacts to the file system.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class RepositoryImpl implements Repository {
    private Map<URI, URL> archiveUriToUrl;
    private File repositoryDir;

    /**
     * Constructor.
     *
     * @param hostInfo the host info for the runtime
     * @throws IOException if an error occurs initializing the repository
     */
    public RepositoryImpl(@Reference HostInfo hostInfo) throws IOException {
        archiveUriToUrl = new ConcurrentHashMap<URI, URL>();
        File baseDir = hostInfo.getBaseDir();
        // three locations for artifacts: user; extensions; and a temporary cache
        repositoryDir = new File(baseDir, "repository");
    }

    @Init
    public void init() throws MalformedURLException {
        if (!repositoryDir.exists() || !repositoryDir.isDirectory()) {
            return;
        }
        // load artifacts
        for (File file : repositoryDir.listFiles()) {
            archiveUriToUrl.put(mapToUri(file), file.toURI().toURL());
        }
    }

    public URL store(URI uri, InputStream stream) throws RepositoryException {
        try {
            if (!repositoryDir.exists() || !repositoryDir.isDirectory() || !repositoryDir.canRead()) {
                throw new IOException("The repository location is not a directory: " + repositoryDir);
            }
            File location = mapToFile(repositoryDir, uri);
            FileHelper.write(stream, location);
            URL locationUrl = location.toURL();
            archiveUriToUrl.put(uri, locationUrl);
            return locationUrl;
        } catch (IOException e) {
            String id = uri.toString();
            throw new RepositoryException("Error storing: " + id, id, e);
        }
    }

    public boolean exists(URI uri) {
        return archiveUriToUrl.containsKey(uri);
    }

    public URL find(URI uri) {
        return archiveUriToUrl.get(uri);
    }

    public void remove(URI uri) throws RepositoryException {
        try {
            File location = mapToFile(repositoryDir, uri);
            archiveUriToUrl.remove(uri);
            location.delete();
        } catch (IOException e) {
            String id = uri.toString();
            throw new RepositoryException("Error removing: " + id, id, e);
        }
    }

    public List<URI> list() {
        return new ArrayList<URI>(archiveUriToUrl.keySet());
    }

    /**
     * Resolve contribution location in the repository.
     *
     * @param base the base repository directory
     * @param uri  the uri to resolve @return the mapped file
     * @return the mapped file
     * @throws IOException if an exception occurs mapping the file
     */
    private File mapToFile(File base, URI uri) throws IOException {
        if (!base.exists() || !base.isDirectory() || !base.canRead()) {
            throw new IOException("The repository location is not a directory: " + base);
        }
        return new File(base, uri.getPath());
    }

    /**
     * Maps a file to a URI.
     *
     * @param file the file
     * @return the URI
     */
    private URI mapToUri(File file) {
        return URI.create(file.getName());
    }

}
