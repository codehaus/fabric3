/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.scanner.scanner;

import java.io.File;
import java.io.IOException;
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
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.contribution.ValidationException;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.scdl.Composite;
import org.fabric3.spi.domain.Domain;
import org.fabric3.spi.plan.DeploymentPlan;
import org.fabric3.spi.scanner.FileSystemResource;
import org.fabric3.spi.scanner.FileSystemResourceFactoryRegistry;
import org.fabric3.spi.services.VoidService;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.services.event.Fabric3EventListener;
import org.fabric3.spi.services.event.RuntimeStart;

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
 * they remain cached. If they have not changed, they are processed, contributed via the ContributionService, and activated in the domain.
 * <p/>
 * Note update and remove are not fully implemented.
 */
@Service(VoidService.class)
@EagerInit
public class ContributionDirectoryScanner implements Runnable, Fabric3EventListener<RuntimeStart> {
    private final Map<String, FileSystemResource> cache = new HashMap<String, FileSystemResource>();
    private final Map<String, FileSystemResource> errorCache = new HashMap<String, FileSystemResource>();
    private final ContributionService contributionService;
    private final EventService eventService;
    private MetaDataStore metaDataStore;
    private final ScannerMonitor monitor;
    private final Domain domain;
    private Map<String, URI> processed = new HashMap<String, URI>();
    private FileSystemResourceFactoryRegistry registry;
    private File path;

    private long delay = 5000;
    private ScheduledExecutorService executor;

    public ContributionDirectoryScanner(@Reference FileSystemResourceFactoryRegistry registry,
                                        @Reference ContributionService contributionService,
                                        @Reference(name = "assembly") Domain domain,
                                        @Reference EventService eventService,
                                        @Reference MetaDataStore metaDataStore,
                                        @Reference HostInfo info,
                                        @Monitor ScannerMonitor monitor) {
        this.registry = registry;
        this.contributionService = contributionService;
        this.domain = domain;
        this.eventService = eventService;
        this.metaDataStore = metaDataStore;
        path = new File(info.getBaseDir(), "deploy");
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setPath(String dir) {
        this.path = new File(dir);
    }

    @Property(required = false)
    public void setDelay(long delay) {
        this.delay = delay;
    }

    @Init
    public void init() {
        // register to be notified when the runtime starts so the scanner thread can be initialized
        eventService.subscribe(RuntimeStart.class, this);
    }

    @Destroy
    public void destroy() {
        executor.shutdownNow();
    }

    public void onEvent(RuntimeStart event) {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this, 10, delay, TimeUnit.MILLISECONDS);
    }

    public synchronized void run() {
        if (!path.isDirectory()) {
            // there is no extension directory, return without processing
            return;
        }
        try {
            File[] files = path.listFiles();
            processRemovals(files);
            processFiles(files);
        } catch (RuntimeException e) {
            monitor.error(e);
        }

    }

    private synchronized void processFiles(File[] files) {
        boolean wait = false;
        List<File> ignored = new ArrayList<File>();
        for (File file : files) {
            try {
                String name = file.getName();
                FileSystemResource resource = null;
                FileSystemResource cached = errorCache.get(name);
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
                    // the file has been added
                    if (resource == null) {
                        resource = registry.createResource(file);
                    }
                    if (resource == null) {
                        // not a known type, ignore
                        ignored.add(file);
                        continue;
                    }
                    resource.reset();
                    // cache the resource and wait to the next run to see if it has changed
                    cache.put(name, resource);
                    wait = true;
                } else {
                    // already cached from a previous run
                    if (cached.isChanged()) {
                        // contents are still being updated, wait until next run
                        wait = true;
                    }
                }
            } catch (IOException e) {
                monitor.error(e);
            }
        }
        if (!wait) {
            sortAndProcessChanges(files, ignored);
        }
    }

    private void sortAndProcessChanges(File[] files, List<File> ignored) {
        try {
            List<File> updates = new ArrayList<File>();
            List<File> additions = new ArrayList<File>();
            for (File file : files) {
                // check if it is in the store
                String name = file.getName();
                boolean isProcessed = processed.containsKey(name);
                boolean isError = errorCache.containsKey(name);
                if (!isError && isProcessed && !ignored.contains(file)) {
                    // updated
                    updates.add(file);
                } else if (!isError && !isProcessed && !ignored.contains(file)) {
                    // an addition
                    additions.add(file);
                }

            }
            processUpdates(updates);
            processAdditions(additions);
        } catch (IOException e) {
            monitor.error(e);
        }
    }

    private synchronized void processUpdates(List<File> files) throws IOException {
        for (File file : files) {
            String name = file.getName();
            URI artifactUri = processed.get(name);
            URL location = file.toURI().normalize().toURL();
            FileSystemResource cached = cache.remove(name);
            byte[] checksum = cached.getChecksum();
            long timestamp = file.lastModified();
            long previousTimestamp = contributionService.getContributionTimestamp(artifactUri);
            if (timestamp > previousTimestamp) {
                try {
                    ContributionSource source = new FileContributionSource(artifactUri, location, timestamp, checksum);
                    contributionService.update(source);
                } catch (ContributionException e) {
                    errorCache.put(name, cached);
                    monitor.error(e);
                }
                monitor.update(artifactUri.toString());
            }
            // TODO undeploy and redeploy
        }
    }

    private synchronized void processAdditions(List<File> files) throws IOException {
        List<ContributionSource> sources = new ArrayList<ContributionSource>();
        List<FileSystemResource> addedResources = new ArrayList<FileSystemResource>();
        for (File file : files) {
            String name = file.getName();
            FileSystemResource cached = cache.remove(name);
            addedResources.add(cached);
            URL location = file.toURI().normalize().toURL();
            byte[] checksum = cached.getChecksum();
            long timestamp = file.lastModified();
            try {
                ContributionSource source = new FileContributionSource(URI.create(name), location, timestamp, checksum);
                sources.add(source);
            } catch (NoClassDefFoundError e) {
                errorCache.put(name, cached);
                monitor.error(e);
            }
        }
        if (!sources.isEmpty()) {
            try {
                // install contributions, which will be ordered transitively by import dependencies
                List<URI> addedUris = contributionService.contribute(sources);
                // activate the contributions by including deployables in a synthesized composite. This will ensure components are started according
                // to dependencies even if a dependent component is defined in a different contribution.
                List<Composite> deployables = getDeployables(addedUris);
                List<DeploymentPlan> plans = getDeploymentPlans(addedUris);
                domain.include(deployables, plans, false);
                for (URI uri : addedUris) {
                    String name = uri.toString();
                    // URI is the file name
                    processed.put(name, uri);
                    monitor.add(name);
                }
            } catch (ValidationException e) {
                // print out the validation errors
                monitor.contributionErrors(e.getMessage());
                // FIXME for now, just error all additions
                for (FileSystemResource cached : addedResources) {
                    errorCache.put(cached.getName(), cached);
                }
            } catch (AssemblyException e) {
                // print out the deployment errors
                monitor.deploymentErrors(e.getMessage());
                // FIXME for now, just error all additions
                for (FileSystemResource cached : addedResources) {
                    errorCache.put(cached.getName(), cached);
                }
            } catch (ContributionException e) {
                // FIXME for now, just error all additions
                for (FileSystemResource cached : addedResources) {
                    errorCache.put(cached.getName(), cached);
                }
                monitor.error(e);
            } catch (DeploymentException e) {
                // FIXME for now, just error all additions
                for (FileSystemResource cached : addedResources) {
                    errorCache.put(cached.getName(), cached);
                }
                monitor.error(e);
            } catch (RuntimeException e) {
                // FIXME for now, just error all additions
                for (FileSystemResource cached : addedResources) {
                    errorCache.put(cached.getName(), cached);
                }
                monitor.error(e);
                throw e;
            }

        }
    }

    /**
     * Returns the list of deployables from contributions identified by the list of URIs
     *
     * @param contributionUris the contributions containing the deployables
     * @return the list of deployables
     */
    private List<Composite> getDeployables(List<URI> contributionUris) {
        List<Composite> deployables = new ArrayList<Composite>();
        for (URI uri : contributionUris) {
            Contribution contribution = metaDataStore.find(uri);
            assert contribution != null;
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> entry : resource.getResourceElements()) {
                    if (!(entry.getValue() instanceof Composite)) {
                        continue;
                    }
                    @SuppressWarnings({"unchecked"})
                    ResourceElement<QNameSymbol, Composite> element = (ResourceElement<QNameSymbol, Composite>) entry;
                    QName name = element.getSymbol().getKey();
                    Composite composite = element.getValue();
                    for (Deployable deployable : contribution.getManifest().getDeployables()) {
                        if (deployable.getName().equals(name)) {
                            deployables.add(composite);
                            break;
                        }
                    }
                }
            }
        }
        return deployables;
    }

    private List<DeploymentPlan> getDeploymentPlans(List<URI> contributionUris) {
        List<DeploymentPlan> plans = new ArrayList<DeploymentPlan>();
        for (URI uri : contributionUris) {
            Contribution contribution = metaDataStore.find(uri);
            assert contribution != null;
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> entry : resource.getResourceElements()) {
                    if (!(entry.getValue() instanceof DeploymentPlan)) {
                        continue;
                    }
                    @SuppressWarnings({"unchecked"})
                    ResourceElement<QNameSymbol, DeploymentPlan> element = (ResourceElement<QNameSymbol, DeploymentPlan>) entry;
                    DeploymentPlan plan = element.getValue();
                    plans.add(plan);
                }
            }
        }
        return plans;
    }

    private synchronized void processRemovals(File[] files) {
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

}
