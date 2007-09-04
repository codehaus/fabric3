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
package org.fabric3.runtime.webapp;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.servlet.ServletContext;

import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.fabric.assembly.DistributedAssembly;
import org.fabric3.fabric.runtime.ComponentNames;
import static org.fabric3.fabric.runtime.ComponentNames.CONTRIBUTION_SERVICE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.DEFINITIONS_DEPLOYER;
import static org.fabric3.fabric.runtime.ComponentNames.DISCOVERY_SERVICE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.DISTRIBUTED_ASSEMBLY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.SCOPE_REGISTRY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.WORK_SCHEDULER_URI;
import org.fabric3.fabric.runtime.ExtensionInitializationException;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.CoordinatorMonitor;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.runtime.StartException;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.services.definitions.DefinitionActivationException;
import org.fabric3.spi.services.definitions.DefinitionsDeployer;
import org.fabric3.spi.services.discovery.DiscoveryException;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.spi.services.work.WorkScheduler;

/**
 * Implementation of a coordinator for the webapp runtime.
 *
 * @version $Rev$ $Date$
 */
public class WebappCoordinator implements RuntimeLifecycleCoordinator<WebappRuntime, Bootstrapper> {
    private static final String EXTENSIONS = "extensions";
    public static final String EXTENSIONS_DIR = "/WEB-INF/fabric3/extensions";

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
    private WebappRuntime runtime;
    private Bootstrapper bootstrapper;
    private WorkScheduler scheduler;
    private CoordinatorMonitor monitor;

    public void bootPrimordial(WebappRuntime runtime,
                               Bootstrapper bootstrapper,
                               ClassLoader bootClassLoader,
                               ClassLoader appClassLoader) throws InitializationException {
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
            URI systemGroupId = URI.create(ComponentNames.RUNTIME_NAME + "/");
            WorkContext workContext = new SimpleWorkContext();
            workContext.setScopeIdentifier(Scope.COMPOSITE, systemGroupId);
            container.startContext(workContext, systemGroupId);

            // start the domain context
            URI domainUri = runtime.getHostInfo().getDomain();
            URI groupId = URI.create(domainUri.toString() + "/");
            workContext = new SimpleWorkContext();
            workContext.setScopeIdentifier(Scope.COMPOSITE, groupId);
            container.startContext(workContext, groupId);
        } catch (GroupInitializationException e) {
            throw new InitializationException(e);
        }
        state = State.PRIMORDIAL;
    }

    public void initialize() throws InitializationException {
        if (state != State.PRIMORDIAL) {
            throw new IllegalStateException("Not in PRIMORDIAL state");
        }
        bootstrapper.bootSystem(runtime);
        monitor = runtime.getMonitorFactory().getMonitor(CoordinatorMonitor.class);
        scheduler = runtime.getSystemComponent(WorkScheduler.class, WORK_SCHEDULER_URI);
        if (scheduler == null) {
            state = State.ERROR;
            throw new InitializationException("WorkScheduler not found", WORK_SCHEDULER_URI.toString());
        }

        try {
            includeExtensions();
        } catch (DefinitionActivationException e) {
            throw new InitializationException(e);
        }
        
        state = State.INITIALIZED;

    }

    public Future<Void> joinDomain(final long timeout) {
        if (state != State.INITIALIZED) {
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
     * Processes extensions and includes them in the runtime domain
     *
     * @throws InitializationException if an error occurs included the extensions
     */
    private void includeExtensions() throws InitializationException, DefinitionActivationException {
        ServletContext context = runtime.getHostInfo().getServletContext();
        Set paths = context.getResourcePaths(EXTENSIONS_DIR);
        if (paths == null) {
            return;
        }
        List<URL> files = new ArrayList<URL>();
        for (Object path : paths) {
            String str = (String) path;
            if (str.endsWith(".jar") || str.endsWith(".zip")) {
                try {
                    files.add(context.getResource(str).toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                } catch (URISyntaxException e) {
                    throw new AssertionError(e);
                }
            }
        }
        if (!files.isEmpty()) {
            // contribute and activate extensions if they exist in the runtime domain
            ContributionService contributionService = runtime.getSystemComponent(ContributionService.class,
                                                                                 CONTRIBUTION_SERVICE_URI);
            List<URI> contributionUris = new ArrayList<URI>();
            for (URL file : files) {
                try {
                    ContributionSource source = new FileContributionSource(file, -1, new byte[0]);
                    contributionUris.add(contributionService.contribute(EXTENSIONS, source));
                } catch (ContributionException e) {
                    throw new ExtensionInitializationException("Error loading extension", file.toString(), e);
                }

            }
            runtime.includeExtensionContributions(contributionUris);
            DefinitionsDeployer definitionsDeployer =
                runtime.getSystemComponent(DefinitionsDeployer.class, DEFINITIONS_DEPLOYER);
            definitionsDeployer.activateDefinitions(contributionUris);
        }
    }


}
