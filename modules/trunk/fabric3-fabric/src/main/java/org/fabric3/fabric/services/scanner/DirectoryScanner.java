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
package org.fabric3.fabric.services.scanner;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.thoughtworks.xstream.XStream;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.fabric.services.xstream.XStreamFactory;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.spi.services.VoidService;
import org.fabric3.spi.services.scanner.DestinationException;
import org.fabric3.spi.services.scanner.DirectoryScannerDestination;
import org.fabric3.spi.services.scanner.FileSystemResource;
import org.fabric3.spi.services.scanner.FileSystemResourceFactoryRegistry;
import org.fabric3.spi.services.scanner.ResourceMetaData;

/**
 * Periodically scans a directory for files
 */
@Service(VoidService.class)
@EagerInit
public class DirectoryScanner implements Runnable {
    private final Map<String, FileSystemResource> cache = new HashMap<String, FileSystemResource>();
    private final Map<String, FileSystemResource> errorCache = new HashMap<String, FileSystemResource>();
    private final DirectoryScannerDestination destination;
    private final DirectoryScannerMonitor monitor;
    private final XStream xstream;
    private final File processedIndex;
    private Map<String, URI> processed = new HashMap<String, URI>();
    private FileSystemResourceFactoryRegistry registry;
    private String path = "../deploy";

    private long delay = 5000;
    private ScheduledExecutorService executor;

    @Constructor
    public DirectoryScanner(@Reference FileSystemResourceFactoryRegistry registry,
                            @Reference DirectoryScannerDestination contributionService,
                            @Reference XStreamFactory xStreamFactory,
                            @Reference MonitorFactory factory) {
        this.registry = registry;
        this.destination = contributionService;
        this.xstream = xStreamFactory.createInstance();
        this.monitor = factory.getMonitor(DirectoryScannerMonitor.class);
        processedIndex = new File(path + "/.processed");
    }

    @Init
    public void init() throws DestinationException {
        executor = Executors.newSingleThreadScheduledExecutor();
        try {
            recover();
        } catch (FileNotFoundException e) {
            throw new DestinationException(e);
        }
        executor.scheduleWithFixedDelay(this, 10, delay, TimeUnit.MILLISECONDS);
    }

    @Destroy
    public void destroy() {
        executor.shutdownNow();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public synchronized void run() {
        File extensionDir = new File(path);
        if (!extensionDir.isDirectory()) {
            // we don't have an extension directory, there's nothing to do
            return;
        }
        try {
            File[] files = extensionDir.listFiles();
            remove(files);
            addAndUpdate(files);
            // persist changes
            save();
        } catch (FileNotFoundException e) {
            monitor.error("Error persisting scanner state", e);
        }

    }

    @SuppressWarnings({"unchecked"})
    synchronized void recover() throws FileNotFoundException {
        File extensionDir = new File(path);
        if (!extensionDir.isDirectory()) {
            // we don't have an extension directory, there's nothing to do
            return;
        }

        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(processedIndex));
            processed = (HashMap<String, URI>) xstream.fromXML(is);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                monitor.error(e);
            }
        }
        File[] files = extensionDir.listFiles();
        remove(files);
        // check for updates and additions
        addAndUpdate(files);
        save();


    }

    private void addAndUpdate(File[] files) {
        for (File file : files) {
            try {
                String name = file.getName();
                FileSystemResource cached;
                FileSystemResource resource = null;
                cached = errorCache.get(name);
                if (cached != null) {
                    resource = registry.createResource(file);
                    assert resource != null;
                    if (cached.getChecksum().equals(resource)) {
                        // corrupt file from a previous run, continue
                        continue;
                    } else {
                        // file has changed since the error was reported, retry
                        errorCache.remove(name);
                    }
                }
                cached = cache.get(name);
                if (cached == null) {
                    if (resource == null) {
                        resource = registry.createResource(file);
                    }
                    if (resource == null) {
                        // not a known type, ignore
                        continue;
                    }
                    resource.reset();
                    // cache the resource and wait to the next run to see if it has changed
                    cache.put(name, resource);
                } else {
                    // cached
                    if (cached.isChanged()) {
                        // contents are still being updated, wait until next run
                        continue;
                    }
                    cache.remove(name);
                    // check if it is in the store
                    URI artifactUri = processed.get(name);
                    if (artifactUri != null) {
                        // updated
                        URL location = file.toURI().toURL();
                        byte[] checksum = cached.getChecksum();
                        long timestamp = file.lastModified();
                        ResourceMetaData metaData = destination.getResourceMetaData(artifactUri);
                        assert metaData != null;
                        long archivedTimestamp = metaData.getTimestamp();
                        if (timestamp > archivedTimestamp) {
                            destination.updateResource(artifactUri, location, checksum, timestamp);
                        } else if (timestamp == archivedTimestamp && checksum.equals(metaData.getChecksum())) {
                            destination.updateResource(artifactUri, location, checksum, timestamp);
                        }
                    } else {
                        // added
                        URL location = file.toURI().toURL();
                        byte[] checksum = cached.getChecksum();
                        long timestamp = file.lastModified();
                        URI addedUri = destination.addResource(location, checksum, timestamp);
                        processed.put(name, addedUri);
                    }

                }
            } catch (IOException e) {
                monitor.error(e);
            } catch (DestinationException e) {
                monitor.error(e);
            }
        }
    }

    private void remove(File[] files) {
        Map<String, File> index = new HashMap<String, File>(files.length);
        for (File file : files) {
            index.put(file.getName(), file);
        }

        List<String> removed = new ArrayList<String>();
        for (Map.Entry<String, URI> entry : processed.entrySet()) {
            String filename = entry.getKey();
            URI destinationUri = entry.getValue();
            if (index.get(filename) == null) {
                // artifact was removed
                try {
                    // check that the resurce was not deleted by another process
                    if (destination.resourceExists(destinationUri)) {
                        destination.removeResource(destinationUri);
                    }
                    removed.add(filename);
                } catch (DestinationException e) {
                    monitor.error("Error removing artifact", filename, e);
                }
            }
        }
        for (String removedName : removed) {
            processed.remove(removedName);
        }
    }

    /**
     * Persists the list of processed resources for recovery
     *
     * @throws FileNotFoundException if an error occurs opening the persisted file
     */
    private synchronized void save() throws FileNotFoundException {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(processedIndex));
            xstream.toXML(processed, os);

        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    monitor.error(e);
                }
            }
        }
    }
}