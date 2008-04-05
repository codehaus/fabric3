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
package org.fabric3.runtime.standalone.host;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.xml.namespace.QName;

import org.fabric3.fabric.assembly.DistributedAssembly;
import org.fabric3.fabric.runtime.ComponentNames;
import static org.fabric3.fabric.runtime.ComponentNames.CONTRIBUTION_SERVICE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.DEFINITIONS_REGISTRY;
import static org.fabric3.fabric.runtime.ComponentNames.DISCOVERY_SERVICE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.DISTRIBUTED_ASSEMBLY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.METADATA_STORE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_ASSEMBLY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.SCOPE_REGISTRY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.WORK_SCHEDULER_URI;
import org.fabric3.fabric.runtime.ExtensionInitializationException;
import org.fabric3.fabric.services.contribution.manifest.XmlManifestProcessor;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.CoordinatorMonitor;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.runtime.StartException;
import org.fabric3.runtime.standalone.StandaloneRuntime;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Include;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.assembly.Assembly;
import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.MetaDataStoreException;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.definitions.DefinitionActivationException;
import org.fabric3.spi.services.definitions.DefinitionsRegistry;
import org.fabric3.spi.services.discovery.DiscoveryException;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.spi.services.work.WorkScheduler;

/**
 * Implementation of a coordinator for the standalone runtime.
 *
 * @version $Rev$ $Date$
 */
public class StandaloneCoordinator implements RuntimeLifecycleCoordinator<StandaloneRuntime, Bootstrapper> {
    private static final String INTENTS_FILE = "intents.xml";
    private ClassLoader bootClassLoader;

    private enum State {
        UNINITIALIZED,
        PRIMORDIAL,
        INITIALIZED,
        DOMAIN_JOINED,
        RECOVERED,
        STARTED,
        SHUTTINGDOWN,
        SHUTDOWN,
        ERROR
    }

    private State state = State.UNINITIALIZED;
    private File extensionsDirectory;
    private StandaloneRuntime runtime;
    private Bootstrapper bootstrapper;
    private WorkScheduler scheduler;
    private CoordinatorMonitor monitor;

    public void bootPrimordial(StandaloneRuntime runtime,
                               Bootstrapper bootstrapper,
                               ClassLoader bootClassLoader,
                               ClassLoader appClassLoader) throws InitializationException {
        this.bootClassLoader = bootClassLoader;
        if (state != State.UNINITIALIZED) {
            throw new IllegalStateException("Not in UNINITIALIZED state");
        }
        this.runtime = runtime;
        this.bootstrapper = bootstrapper;
        try {
            runtime.initialize();
            bootstrapper.bootPrimordial(runtime, bootClassLoader, appClassLoader);

            ScopeRegistry scopeRegistry = runtime.getSystemComponent(ScopeRegistry.class, SCOPE_REGISTRY_URI);
            ScopeContainer<URI> container = scopeRegistry.getScopeContainer(Scope.COMPOSITE);

            // start the system context
            WorkContext workContext = new WorkContext();
            CallFrame frame = new CallFrame(ComponentNames.RUNTIME_URI);
            workContext.addCallFrame(frame);
            container.startContext(workContext);
            workContext.popCallFrame();
            // start the domain context
            URI groupId = runtime.getHostInfo().getDomain();
            workContext = new WorkContext();
            frame = new CallFrame(groupId);
            workContext.addCallFrame(frame);
            container.startContext(workContext);
            extensionsDirectory = new File(runtime.getHostInfo().getInstallDirectory(), "extensions");
        } catch (GroupInitializationException e) {
            state = State.ERROR;
            throw new InitializationException(e);
        }
        state = State.PRIMORDIAL;
    }

    public void initialize() throws InitializationException {
        if (state != State.PRIMORDIAL) {
            state = State.ERROR;
            throw new IllegalStateException("Not in PRIMORDIAL state");
        }
        try {
            bootstrapper.bootSystem(runtime);
            monitor = runtime.getMonitorFactory().getMonitor(CoordinatorMonitor.class);
            scheduler = runtime.getSystemComponent(WorkScheduler.class, WORK_SCHEDULER_URI);
            if (scheduler == null) {
                state = State.ERROR;
                throw new InitializationException("WorkScheduler not found", WORK_SCHEDULER_URI.toString());
            }
            synthesizeSPIContribution();
            activateIntents();
            includeExtensions();
            state = State.INITIALIZED;
        } catch (InitializationException e) {
            state = State.ERROR;
            throw e;
        }
    }

    public Future<Void> joinDomain(final long timeout) {
        if (state != State.INITIALIZED) {
            state = State.ERROR;
            throw new IllegalStateException("Not in INITIALIZED state");
        }
        Callable<Void> callable = new Callable<Void>() {
            public Void call() throws DiscoveryException, InitializationException {
                try {
                    DiscoveryService service =
                            runtime.getSystemComponent(DiscoveryService.class, DISCOVERY_SERVICE_URI);
                    if (service == null) {
                        String identifier = DISCOVERY_SERVICE_URI.toString();
                        throw new InitializationException("Discovery service not found", identifier);
                    }
                    service.joinDomain(timeout);
                    state = State.DOMAIN_JOINED;
                } catch (DiscoveryException e) {
                    state = State.ERROR;
                    monitor.error(e);
                    throw e;
                }

                return null;
            }
        };
        FutureTask<Void> task = new FutureTask<Void>(callable);
        scheduler.scheduleWork(task);
        return task;
    }

    public Future<Void> recover() {
        if (state != State.DOMAIN_JOINED) {
            state = State.ERROR;
            throw new IllegalStateException("Not in DOMAIN_JOINED state");
        }
        Callable<Void> callable = new Callable<Void>() {
            public Void call() throws AssemblyException, InitializationException {
                try {
                    DistributedAssembly assembly =
                            runtime.getSystemComponent(DistributedAssembly.class, DISTRIBUTED_ASSEMBLY_URI);
                    if (assembly == null) {
                        throw new InitializationException("Assembly not found", DISTRIBUTED_ASSEMBLY_URI.toString());
                    }
                    assembly.initialize();
                    state = State.RECOVERED;
                } catch (InitializationException e) {
                    state = State.ERROR;
                    monitor.error(e);
                    throw e;
                } catch (AssemblyException e) {
                    state = State.ERROR;
                    monitor.error(e);
                    throw e;
                }
                return null;
            }
        };
        FutureTask<Void> task = new FutureTask<Void>(callable);
        scheduler.scheduleWork(task);
        return task;
    }

    public Future<Void> start() {
        if (state != State.RECOVERED) {
            state = State.ERROR;
            throw new IllegalStateException("Not in RECOVERED state");
        }
        Callable<Void> callable = new Callable<Void>() {
            public Void call() throws Exception {
                try {
                    runtime.start();
                    state = State.STARTED;
                } catch (StartException e) {
                    state = State.ERROR;
                    monitor.error(e);
                    throw e;
                }
                return null;
            }
        };
        FutureTask<Void> task = new FutureTask<Void>(callable);
        scheduler.scheduleWork(task);
        return task;
    }

    public Future<Void> shutdown() throws ShutdownException {
        if (state != State.STARTED) {
            state = State.ERROR;
            throw new IllegalStateException("Not in STARTED state");
        }
        Callable<Void> callable = new Callable<Void>() {
            public Void call() throws Exception {
                try {
                    runtime.destroy();
                    state = State.SHUTDOWN;
                } catch (ShutdownException e) {
                    state = State.ERROR;
                    monitor.error(e);
                    throw e;
                }
                return null;
            }
        };
        FutureTask<Void> task = new FutureTask<Void>(callable);
        scheduler.scheduleWork(task);
        return task;
    }

    /**
     * Contributes and activates baseline intents as defined by the intents.xml in the active profile directory. If an intents.xml file is not found,
     * the method returns.
     *
     * @throws InitializationException if an error occurs activating the intents
     */
    private void activateIntents() throws InitializationException {
        File dir = runtime.getHostInfo().getProfileDirectory();
        try {
            File file = new File(dir, INTENTS_FILE);
            if (!file.exists()) {
                return;
            }
            ContributionService contributionService = runtime.getSystemComponent(ContributionService.class,
                                                                                 CONTRIBUTION_SERVICE_URI);
            URI contribuUri = file.toURI();
            URL location = contribuUri.toURL();
            ContributionSource source = new FileContributionSource(contribuUri, location, -1, new byte[0]);
            URI uri = contributionService.contribute(source);
            DefinitionsRegistry definitionsRegistry = runtime.getSystemComponent(DefinitionsRegistry.class, DEFINITIONS_REGISTRY);
            List<URI> intents = new ArrayList<URI>();
            intents.add(uri);
            definitionsRegistry.activateDefinitions(intents);
        } catch (MalformedURLException e) {
            throw new InitializationException(e);
        } catch (ContributionException e) {
            throw new InitializationException(e);
        } catch (DefinitionActivationException e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Processes extensions and includes them in the runtime domain
     *
     * @throws InitializationException if an error occurs included the extensions
     */
    private void includeExtensions() throws InitializationException {
        if (extensionsDirectory != null && extensionsDirectory.exists()) {
            // contribute and activate extensions if they exist in the runtime domain
            ContributionService contributionService = runtime.getSystemComponent(ContributionService.class,
                                                                                 CONTRIBUTION_SERVICE_URI);
            List<URI> contributionUris;
            List<ContributionSource> sources = new ArrayList<ContributionSource>();
            File[] files = extensionsDirectory.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    String name = pathname.getName();
                    return name.endsWith(".jar") || name.endsWith(".zip");
                }
            });
            for (File file : files) {
                try {
                    URI uri = file.toURI();
                    ContributionSource source = new FileContributionSource(uri, uri.toURL(), -1, new byte[0]);
                    sources.add(source);
                } catch (MalformedURLException e) {
                    throw new ExtensionInitializationException("Error loading extension", file.getName(), e);
                } catch (IOException e) {
                    throw new ExtensionInitializationException("Error loading extension", file.getName(), e);
                }

            }
            try {
                contributionUris = contributionService.contribute(sources);
            } catch (ContributionException e) {
                throw new ExtensionInitializationException("Error loading extension", e);
            }
            includeExtensionContributions(contributionUris);
        }
    }

    /*
     FIXME this code was in AbstractRuntime but isn't really runtime functionality
     FIXME it is now duplicated in all coordinators and should be refactored into one place
     */
    public void includeExtensionContributions(List<URI> contributionUris) throws InitializationException {
        Assembly assembly = runtime.getSystemComponent(Assembly.class, RUNTIME_ASSEMBLY_URI);
        Composite composite = createExtensionComposite(contributionUris);
        try {
            assembly.includeInDomain(composite);
        } catch (ActivateException e) {
            throw new ExtensionInitializationException("Error activating extensions", e);
        }
    }

    /**
     * Creates an extension composite by including deployables from contributions identified by the list of URIs
     *
     * @param contributionUris the contributions containing the deployables to include
     * @return the extension composite
     * @throws InitializationException if an error occurs creating the composite
     */
    private Composite createExtensionComposite(List<URI> contributionUris) throws InitializationException {
        MetaDataStore metaDataStore = runtime.getSystemComponent(MetaDataStore.class, METADATA_STORE_URI);
        if (metaDataStore == null) {
            String id = METADATA_STORE_URI.toString();
            throw new InitializationException("Extensions metadata store not configured", id);
        }
        QName qName = new QName(org.fabric3.spi.Constants.FABRIC3_SYSTEM_NS, "extension");
        Composite composite = new Composite(qName);
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
                    Composite childComposite = element.getValue();
                    for (Deployable deployable : contribution.getManifest().getDeployables()) {
                        if (deployable.getName().equals(name)) {
                            Include include = new Include();
                            include.setName(name);
                            include.setIncluded(childComposite);
                            composite.add(include);
                            break;
                        }
                    }
                }
            }
        }
        return composite;
    }

    private void synthesizeSPIContribution() throws InitializationException {
        try {
            Contribution contribution = new Contribution(ComponentNames.BOOT_CLASSLOADER_ID);
            ContributionManifest manifest = new ContributionManifest();
            InputStream stream =
                    bootClassLoader.getResourceAsStream("META-INF/maven/org.codehaus.fabric3/fabric3-spi/pom.xml");
            if (stream == null) {
                throw new InitializationException("fabric3-spi jar is missing pom.xml file");
            }
            XmlManifestProcessor processor =
                    runtime.getSystemComponent(XmlManifestProcessor.class, ComponentNames.XML_MANIFEST_PROCESSOR);
            processor.process(manifest, stream);
            stream = bootClassLoader.getResourceAsStream("META-INF/maven/org.codehaus.fabric3/fabric3-pojo/pom.xml");
            if (stream == null) {
                throw new InitializationException("fabric3-pojo jar is missing pom.xml file");
            }
            processor.process(manifest, stream);
            contribution.setManifest(manifest);
            MetaDataStore store = runtime.getSystemComponent(MetaDataStore.class, ComponentNames.METADATA_STORE_URI);
            store.store(contribution);
        } catch (MetaDataStoreException e) {
            throw new InitializationException(e);
        } catch (ContributionException e) {
            throw new InitializationException(e);
        }
    }


}
