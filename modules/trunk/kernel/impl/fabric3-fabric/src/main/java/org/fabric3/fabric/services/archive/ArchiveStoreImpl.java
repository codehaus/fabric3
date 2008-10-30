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
package org.fabric3.fabric.services.archive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.archive.ArchiveStore;
import org.fabric3.spi.services.archive.ArchiveStoreException;

/**
 * The default implementation of ArchiveStore
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ArchiveStoreImpl implements ArchiveStore {
    private Map<URI, URL> archiveUriToUrl;
    private File baseDir;

    /**
     * Constructor.
     *
     * @param hostInfo the host info for the runtime
     * @throws IOException if an error occurs initializing the repository
     */
    public ArchiveStoreImpl(@Reference HostInfo hostInfo) throws IOException {
        archiveUriToUrl = new ConcurrentHashMap<URI, URL>();
        baseDir = hostInfo.getBaseDir();
    }


    public URL store(URI uri, InputStream stream) throws ArchiveStoreException {
        try {
            File root = new File(baseDir, "repository");
            if (!root.exists() || !root.isDirectory() || !root.canRead()) {
                throw new IOException("The repository location is not a directory: " + root);
            }
            File location = mapToFile(uri);
            write(stream, location);
            URL locationUrl = location.toURL();
            archiveUriToUrl.put(uri, locationUrl);
            return locationUrl;
        } catch (IOException e) {
            String id = uri.toString();
            throw new ArchiveStoreException("Error storing archive: " + id, id, e);
        }
    }

    public boolean exists(URI uri) {
        return archiveUriToUrl.containsKey(uri);
    }

    public URL find(URI uri) {
        return archiveUriToUrl.get(uri);
    }

    public void remove(URI uri) throws ArchiveStoreException {
        try {
            File location = mapToFile(uri);
            archiveUriToUrl.remove(uri);
            location.delete();
        } catch (IOException e) {
            String id = uri.toString();
            throw new ArchiveStoreException("Error storing archive: " + id, id, e);
        }
    }

    public List<URI> list() {
        return new ArrayList<URI>(archiveUriToUrl.keySet());
    }

    /**
     * Resolve contribution location in the repository
     *
     * @param uri the uri to resolve
     * @return the mapped file
     * @throws IOException if an exception occurs mapping the file
     */
    private File mapToFile(URI uri) throws IOException {
        File root = new File(baseDir, "repository");
        if (!root.exists() || !root.isDirectory() || !root.canRead()) {
            throw new IOException("The repository location is not a directory: " + root);
        }
        return new File(root, uri.getPath());
    }

    private void write(InputStream source, File target) throws IOException {
        RandomAccessFile file = new RandomAccessFile(target, "rw");
        FileChannel channel = null;
        FileLock lock = null;
        try {
            channel = file.getChannel();
            lock = channel.lock();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            byte[] bytes = buffer.array();
            int limit;
            while (-1 != (limit = source.read(bytes))) {
                buffer.flip();
                buffer.limit(limit);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                buffer.clear();
            }
            channel.force(true);
        } finally {
            if (channel != null) {
                if (lock != null) {
                    lock.release();
                }
                channel.close();
            }
            file.close();
        }

    }
}
