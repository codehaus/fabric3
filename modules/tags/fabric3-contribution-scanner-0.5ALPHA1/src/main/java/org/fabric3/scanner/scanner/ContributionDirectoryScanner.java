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
package org.fabric3.scanner.scanner;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.assembly.Assembly;
import org.fabric3.spi.services.marshaller.MarshalException;
import org.fabric3.spi.services.marshaller.MarshalService;
import org.fabric3.spi.scanner.FileSystemResource;
import org.fabric3.spi.scanner.FileSystemResourceFactoryRegistry;
import org.fabric3.spi.services.VoidService;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.services.event.Fabric3Event;
import org.fabric3.spi.services.event.Fabric3EventListener;
import org.fabric3.spi.services.event.RuntimeStart;
import org.fabric3.services.xmlfactory.XMLFactory;

/**
 * Periodically scans a directory for new, updated, or removed contributions. New contributions are added to the domain and any deployable components
 * activated. Updated components will trigger re-activation of previously deployed components. Removal will remove the contribution from the domain
 * and de-activate any associated deployed components.
 * <p/>
 * The scanner watches the deployment directory at a fixed-delay interval. Files are tracked as a {@link FileSystemResource}, which provides a
 * consistent metadata view across various types such as jars and exploded directories. Unknown file types are ignored. At the specified interval,
 * removed files are determined by comparing the current directory contents with the contents from the previous pass. Changes or additions are also
 * determined by comparing the current directory state with that of the previous pass. Detected changes and additions are cached for the following
 * interval. Detected changes and additions from the previous interval are then checked using a checksum to see if they have changed again. If so,
 * they remain cached. If they have not changed, they are processed, contributed via the ContributionService and activated in the domain.
 * <p/>
 * The scanner is persistent and supports recovery on re-start.
 * <p/>
 * Note update and remove are not fully implemented.
 */
@Service(VoidService.class)
@EagerInit
public class ContributionDirectoryScanner implements Runnable, Fabric3EventListener {
    private final Map<String, FileSystemResource> cache = new HashMap<String, FileSystemResource>();
    private final Map<String, FileSystemResource> errorCache = new HashMap<String, FileSystemResource>();
    private final ContributionService contributionService;
    private final EventService eventService;
    private final MarshalService marshallService;
    private final XMLInputFactory xmlInputFactory;
    private final XMLOutputFactory xmlOutputFactory;
    private final ScannerMonitor monitor;
    private final Assembly assembly;
    private Map<String, URI> processed = new HashMap<String, URI>();
    private FileSystemResourceFactoryRegistry registry;
    private String path = "../deploy";
    private File processedIndex;
    private boolean persistent = true;

    private long delay = 5000;
    private ScheduledExecutorService executor;

    public ContributionDirectoryScanner(@Reference FileSystemResourceFactoryRegistry registry,
                                        @Reference ContributionService contributionService,
                                        @Reference(name = "assembly")Assembly assembly,
                                        @Reference EventService eventService,
                                        @Reference MarshalService service,
                                        @Reference XMLFactory xmlFactory,
                                        @Monitor ScannerMonitor monitor) {
        this.registry = registry;
        this.contributionService = contributionService;
        this.assembly = assembly;
        this.eventService = eventService;
        this.marshallService = service;
        this.xmlInputFactory = xmlFactory.newInputFactoryInstance();
        this.xmlOutputFactory = xmlFactory.newOutputFactoryInstance();
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setPath(String path) {
        this.path = path;
    }

    @Property(required = false)
    public void setDelay(long delay) {
        this.delay = delay;
    }

    @Property(required = false)
    // JFM fix me when boolean types supported
    public void setPersistent(String persistent) {
        this.persistent = Boolean.valueOf(persistent);
    }

    @Init
    public void init() {
        processedIndex = new File(path + "/.processed");
        // register to be notified when the runtime starts so the scanner thread can be initialized
        eventService.subscribe(RuntimeStart.class, this);
    }


    public void onEvent(Fabric3Event event) {
        executor = Executors.newSingleThreadScheduledExecutor();
        try {
            recover();
            executor.scheduleWithFixedDelay(this, 10, delay, TimeUnit.MILLISECONDS);
        } catch (FileNotFoundException e) {
            monitor.recoveryError(e);
        } catch (MarshalException e) {
            monitor.recoveryError(e);
        } catch (XMLStreamException e) {
            monitor.recoveryError(e);
        }
    }

    @Destroy
    public void destroy() {
        executor.shutdownNow();
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
            monitor.recoveryError(e);
        } catch (RuntimeException e) {
            monitor.error(e);
        } catch (MarshalException e) {
            monitor.error(e);
        } catch (XMLStreamException e) {
            monitor.error(e);
        }

    }

    @SuppressWarnings({"unchecked"})
    synchronized void recover() throws FileNotFoundException, XMLStreamException, MarshalException {
        File extensionDir = new File(path);
        if (!extensionDir.isDirectory()) {
            // we don't have an extension directory, there's nothing to do
            return;
        }
        if (!processedIndex.exists()) {
            // no index which means there is nothing to recover
            return;
        }
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(processedIndex));
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(is);
            processed = marshallService.unmarshall(HashMap.class, reader);
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
                    resource.reset();
                    if (Arrays.equals(cached.getChecksum(), resource.getChecksum())) {
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
                    processCachedResource(name, file, cached);
                }
            } catch (IOException e) {
                monitor.error(e);
            }
        }
    }

    private void processCachedResource(String name, File file, FileSystemResource cached) throws IOException {
        cache.remove(name);
        // check if it is in the store
        URI artifactUri = processed.get(name);
        URL location = file.toURI().normalize().toURL();
        byte[] checksum = cached.getChecksum();
        long timestamp = file.lastModified();
        if (artifactUri != null) {
            // updated
            long previousTimestamp = contributionService.getContributionTimestamp(artifactUri);
            if (timestamp > previousTimestamp) {
                try {
                    ContributionSource source = new FileContributionSource(artifactUri, location, timestamp, checksum);
                    contributionService.update(source);
                } catch (ContributionException e) {
                    errorCache.put(name, cached);
                    monitor.error(e);
                }
            }
            monitor.update(location.toString());
            // TODO undeploy and redeploy
        } else {
            // added
            try {
                ContributionSource source = new FileContributionSource(location, timestamp, checksum);
                URI addedUri = contributionService.contribute(source);
                List<Deployable> deployables = contributionService.getDeployables(addedUri);
                for (Deployable deployable : deployables) {
                    if (Constants.COMPOSITE_TYPE.equals(deployable.getType())) {
                        // include composite deployables at the domain level
                        assembly.includeInDomain(deployable.getName());
                    }
                }
                processed.put(name, addedUri);
                monitor.add(file.getName());
            } catch (ContributionException e) {
                errorCache.put(name, cached);
                monitor.error(e);
            } catch (ActivateException e) {
                errorCache.put(name, cached);
                monitor.error(e);
            } catch (NoClassDefFoundError e) {
                errorCache.put(name, cached);
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
            URI uri = entry.getValue();
            if (index.get(filename) == null) {
                // artifact was removed
                try {
                    // check that the resurce was not deleted by another process
                    if (contributionService.exists(uri)) {
                        // TODO get Deployables and remove from assembly
                        contributionService.remove(uri);
                    }
                    removed.add(filename);
                    monitor.remove(filename);
                } catch (ContributionException e) {
                    monitor.removalError(filename, e);
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
     * @throws XMLStreamException    if an error occurs creating the XML stream
     * @throws MarshalException      if an error occurs marshalling
     */
    private synchronized void save() throws FileNotFoundException, XMLStreamException, MarshalException {
        if (!persistent) {
            return;
        }
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(processedIndex));
            XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(os);
            marshallService.marshall(processed, writer);
        } catch (Error e) {
            e.printStackTrace();
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
