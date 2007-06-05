/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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

package org.fabric3.fabric.services.contribution;

import java.io.File;
import static java.io.File.separator;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.contribution.ArchiveStore;
import org.fabric3.spi.services.contribution.ContributionStoreRegistry;

/**
 * The default implementation of ContributionStore
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ArchiveStoreImpl implements ArchiveStore {
    private final ContributionStoreRegistry registry;
    private final File root;
    private Map<URI, URL> contributionUriToUrl;
    private String storeId = DEFAULT_STORE;

    /**
     * Creates a new repository service instance
     *
     * @param repository the repository location
     * @param registry   the contribution store registry this store will register with
     * @param hostInfo   the host info for the runtime
     * @throws java.io.IOException if an error occurs initializing the repository
     */
    public ArchiveStoreImpl(
            @Property(name = "repository")String repository,
            @Reference ContributionStoreRegistry registry,
            @Reference HostInfo hostInfo)
            throws IOException {
        this.registry = registry;
        if (repository == null) {
            final String domain = ContributionUtil.getDomainPath(hostInfo.getDomain());
            repository = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    // Default to <user.home>/.fabric3/domains/<domain>/
                    String userHome = System.getProperty("user.home");
                    return userHome + separator + ".fabric3" + separator + "domains" + separator + domain + separator;
                }
            });
        }
        root = new File(repository);
        FileHelper.forceMkdir(root);
        if (!root.exists() || !this.root.isDirectory() || !root.canRead()) {
            throw new IOException("The repository location is not a directory: " + repository);
        }
        contributionUriToUrl = new ConcurrentHashMap<URI, URL>();
    }

    @Constructor
    @Deprecated
    // JFM FIXME remove when properties work
    public ArchiveStoreImpl(@Reference HostInfo hostInfo, @Reference ContributionStoreRegistry registry)
            throws IOException {
        this.registry = registry;
        // FIXME repository mapping should include the domain name
        final String domain = ContributionUtil.getDomainPath(hostInfo.getDomain());
        String repository = AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                // Default to <user.home>/.fabric3/domains/<domain>/
                String userHome = System.getProperty("user.home");
                return userHome + separator + ".fabric3" + separator + "domains" + separator + domain + separator;
            }
        });

        root = new File(repository);
        FileHelper.forceMkdir(root);
        if (!root.exists() || !this.root.isDirectory() || !root.canRead()) {
            throw new IOException("The repository location is not a directory: " + repository);
        }
        contributionUriToUrl = new ConcurrentHashMap<URI, URL>();
    }

    @Property(required = false)
    public void setId(String storeId) {
        this.storeId = storeId;
    }

    public String getId() {
        return storeId;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public URL store(URI uri, InputStream stream) throws IOException {
        File location = mapToFile(uri);
        // create the parent directory if necessary
        FileHelper.forceMkdir(location.getParentFile());
        write(stream, location);
        URL locationUrl = location.toURL();
        contributionUriToUrl.put(uri, locationUrl);
        return locationUrl;
    }

    public URL store(URI uri, URL sourceURL) throws IOException {
        // where the file should be stored in the repository
        File location = mapToFile(uri);
        File source = FileHelper.toFile(sourceURL);
        if (source == null || source.isFile()) {
            InputStream stream = sourceURL.openStream();
            try {
                return store(uri, stream);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } else {
            FileHelper.forceMkdir(location);
            FileHelper.copyDirectory(source, location);
            URL locationUrl = location.toURL();
            contributionUriToUrl.put(uri, locationUrl);
            return location.toURL();
        }
    }

    public URL find(URI uri) {
        return contributionUriToUrl.get(uri);
    }

    public void remove(URI uri) {
        throw new UnsupportedOperationException();
    }

    public List<URI> list() {
        return new ArrayList<URI>(contributionUriToUrl.keySet());
    }

    /**
     * Resolve contribution location in the repository -> root repository / contribution file -> contribution group id /
     * artifact id / version
     *
     * @param uri the uri to resolve
     * @return the mapped file
     */
    private File mapToFile(URI uri) {
        // FIXME: Map the contribution URI to a file?
        return new File(root, "contributions" + separator + uri.getPath());
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
            while (-1 != (limit = source.read(bytes))) { // NOPMD
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
