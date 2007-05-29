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
import javax.xml.namespace.QName;

import com.thoughtworks.xstream.XStream;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.fabric.assembly.DistributedAssembly;
import org.fabric3.fabric.assembly.IncludeException;
import org.fabric3.fabric.services.xstream.XStreamFactory;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.spi.services.VoidService;
import org.fabric3.spi.services.scanner.DestinationException;
import org.fabric3.spi.services.scanner.FileSystemResource;
import org.fabric3.spi.services.scanner.FileSystemResourceFactoryRegistry;

/**
 * Periodically scans a directory for new, updated, or removed contributions. New contributions are added to the domain
 * and any deployable components activated. Updated components will trigger re-activation of previously deployed
 * components. Removal will remove the contribution from the domain and de-activate any associated deployed components.
 * <p/>
 * The scanner watches the deployment directory at a fixed-delay interval. Files are tracked as a {@link
 * FileSystemResource}, which provides a consistent metadata view across various types such as jars and exploded
 * directories. Unknown file types are ignored. At the specified interval, removed files are determined by comparing the
 * current directory contents with the contents from the previous pass. Changes or additions are also determined by
 * comparing the current directory state with that of the previous pass. Detected changes and additions are cached for
 * the following interval. Detected changes and additions from the previous interval are then checked using a checksum
 * to see if they have changed again. If so, they remain cached. If they have not changed, they are processed,
 * contributed via the ContributionService and activated in the domain.
 * <p/>
 * The scanner is persistent and supports recovery on re-start.
 * <p/>
 * Note update and remove are not fully implemented.
 */
@Service(VoidService.class)
@EagerInit
public class ContributionDirectoryScanner implements Runnable {
    private final Map<String, FileSystemResource> cache = new HashMap<String, FileSystemResource>();
    private final Map<String, FileSystemResource> errorCache = new HashMap<String, FileSystemResource>();
    private final ContributionService contributionService;
    private final ScannerMonitor monitor;
    private final XStream xstream;
    private final File processedIndex;
    private final DistributedAssembly assembly;
    private Map<String, URI> processed = new HashMap<String, URI>();
    private FileSystemResourceFactoryRegistry registry;
    private String path = "../deploy";

    private long delay = 5000;
    private ScheduledExecutorService executor;

    @Constructor
    public ContributionDirectoryScanner(@Reference FileSystemResourceFactoryRegistry registry,
                                        @Reference ContributionService contributionService,
                                        @Reference DistributedAssembly assembly,
                                        @Reference XStreamFactory xStreamFactory,
                                        @Reference MonitorFactory factory) {
        this.registry = registry;
        this.contributionService = contributionService;
        this.assembly = assembly;
        this.xstream = xStreamFactory.createInstance();
        this.monitor = factory.getMonitor(ScannerMonitor.class);
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
        } catch (RuntimeException e) {
            monitor.error(e);
        }

    }

    @SuppressWarnings({"unchecked"})
    synchronized void recover() throws FileNotFoundException {
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
                    contributionService.update(artifactUri, checksum, timestamp, location);
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
                URI addedUri = contributionService.contribute(location, checksum, timestamp);
                List<QName> deployables = contributionService.getDeployables(addedUri);
                for (QName deployable : deployables) {
                    // include deployables at the domain level
                    assembly.activate(deployable, true);
                }
                processed.put(name, addedUri);
                monitor.add(location.toString());
            } catch (ContributionException e) {
                errorCache.put(name, cached);
                monitor.error(e);
            } catch (IncludeException e) {
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
                        contributionService.remove(uri);
                    }
                    removed.add(filename);
                    monitor.remove(filename);
                } catch (ContributionException e) {
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
     * @throws java.io.FileNotFoundException if an error occurs opening the persisted file
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
