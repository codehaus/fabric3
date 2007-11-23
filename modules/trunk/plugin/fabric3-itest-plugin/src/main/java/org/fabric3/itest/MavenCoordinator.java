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
package org.fabric3.itest;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.maven.artifact.Artifact;

import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.fabric.assembly.DistributedAssembly;
import org.fabric3.fabric.runtime.ComponentNames;
import static org.fabric3.fabric.runtime.ComponentNames.CONTRIBUTION_SERVICE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.DEFINITIONS_DEPLOYER;
import static org.fabric3.fabric.runtime.ComponentNames.DISTRIBUTED_ASSEMBLY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.CONTRIBUTION_STORE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.SCOPE_REGISTRY_URI;
import org.fabric3.fabric.runtime.ExtensionInitializationException;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.runtime.StartException;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.services.archive.ArchiveStore;
import org.fabric3.spi.services.archive.ArchiveStoreException;
import org.fabric3.spi.services.contribution.ContributionConstants;
import org.fabric3.spi.services.definitions.DefinitionActivationException;
import org.fabric3.spi.services.definitions.DefinitionsDeployer;
import org.fabric3.maven.runtime.MavenEmbeddedRuntime;

/**
 * Implementation of a coordinator for the iTest runtime.
 *
 * @version $Rev$ $Date$
 */
public class MavenCoordinator implements RuntimeLifecycleCoordinator<MavenEmbeddedRuntime, Bootstrapper> {
    private static final String EXTENSIONS = "extensions";

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

    private Dependency[] dependencies;
    private State state = State.UNINITIALIZED;
    private MavenEmbeddedRuntime runtime;
    private Bootstrapper bootstrapper;
    private URL intentsLocation;

    /**
     * @param dependencies    Runtime extensions to run in the test.
     * @param intentsLocation the default intents file location or null
     */
    public MavenCoordinator(Dependency[] dependencies, URL intentsLocation) {
        this.dependencies = dependencies;
        this.intentsLocation = intentsLocation;
    }

    public void bootPrimordial(MavenEmbeddedRuntime runtime,
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
        // initialize core system components
        bootstrapper.bootSystem(runtime);

        ContributionService contributionService =
                runtime.getSystemComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);

        try {
            activateIntents();
            includeExtensions(contributionService);
        } catch (DefinitionActivationException e) {
            throw new InitializationException(e);
        }

        state = State.INITIALIZED;

    }

    public Future<Void> joinDomain(final long timeout) {
        if (state != State.INITIALIZED) {
            throw new IllegalStateException("Not in INITIALIZED state");
        }
        state = State.DOMAIN_JOINED;
        // no domain to join
        return new SyncFuture();
    }

    public Future<Void> recover() {
        if (state != State.DOMAIN_JOINED) {
            throw new IllegalStateException("Not in DOMAIN_JOINED state");
        }
        DistributedAssembly assembly =
                runtime.getSystemComponent(DistributedAssembly.class, DISTRIBUTED_ASSEMBLY_URI);
        if (assembly == null) {
            InitializationException e =
                    new InitializationException("Assembly not found", DISTRIBUTED_ASSEMBLY_URI.toString());
            return new SyncFuture(new ExecutionException(e));

        }
        try {
            assembly.initialize();
        } catch (AssemblyException e) {
            return new SyncFuture(new ExecutionException(e));
        }
        state = State.RECOVERED;
        return new SyncFuture();
    }

    public Future<Void> start() {
        if (state != State.RECOVERED) {
            throw new IllegalStateException("Not in RECOVERED state");
        }
        try {
            runtime.start();
            state = State.STARTED;
        } catch (StartException e) {
            state = State.ERROR;
            return new SyncFuture(new ExecutionException(e));
        }
        return new SyncFuture();
    }

    public Future<Void> shutdown() throws ShutdownException {
        if (state != State.STARTED) {
            throw new IllegalStateException("Not in STARTED state");
        }
        runtime.destroy();
        state = State.SHUTDOWN;
        return new SyncFuture();
    }

    private void activateIntents() throws InitializationException {
        try {
            if (intentsLocation == null) {
                return;
            }
            ContributionService contributionService = runtime.getSystemComponent(ContributionService.class,
                                                                                 CONTRIBUTION_SERVICE_URI);
            ContributionSource source = new FileContributionSource(intentsLocation, -1, new byte[0]);
            URI uri = contributionService.contribute(source);
            DefinitionsDeployer deployer = runtime.getSystemComponent(DefinitionsDeployer.class, DEFINITIONS_DEPLOYER);
            List<URI> intents = new ArrayList<URI>();
            intents.add(uri);
            deployer.activateDefinitions(intents);
        } catch (ContributionException e) {
            throw new InitializationException(e);
        } catch (DefinitionActivationException e) {
            throw new InitializationException(e);
        }
    }

    private void includeExtensions(ContributionService contributionService)
            throws InitializationException, DefinitionActivationException {
        if (dependencies != null) {
            ArchiveStore archiveStore = runtime.getSystemComponent(ArchiveStore.class, CONTRIBUTION_STORE_URI);
            if (archiveStore == null) {
                String id = CONTRIBUTION_STORE_URI.toString();
                throw new InitializationException("Extensions archive store not configured", id);
            }
            // contribute and activate extensions if they exist in the runtime domain
            List<URI> contributionUris = new ArrayList<URI>();
            for (Dependency dependency : dependencies) {
                // create a uri from the dependency
                String contribution;
                if (dependency.getVersion() == null) {
                    contribution =
                            dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + Artifact.RELEASE_VERSION;

                } else {
                    contribution =
                            dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion();
                }
                try {
                    URL url = archiveStore.find(URI.create(contribution));
                    if (url == null) {
                        throw new ExtensionInitializationException("Extension not found in Maven Repository",
                                                                   contribution);
                    }
                    ContributionSource source = new FileContributionSource(url, -1, new byte[0]);
                    contributionUris.add(contributionService.contribute(EXTENSIONS, source));
                } catch (ArchiveStoreException e) {
                    throw new ExtensionInitializationException("Error contributing extension", contribution, e);
                } catch (ContributionException e) {
                    throw new ExtensionInitializationException("Error contributing extension", contribution, e);
                }
            }
            runtime.includeExtensionContributions(contributionUris);
            DefinitionsDeployer definitionsDeployer =
                    runtime.getSystemComponent(DefinitionsDeployer.class, DEFINITIONS_DEPLOYER);
            definitionsDeployer.activateDefinitions(contributionUris);
        }
    }

    private static class SyncFuture implements Future<Void> {
        private ExecutionException ex;

        public SyncFuture() {
        }

        public SyncFuture(ExecutionException ex) {
            this.ex = ex;
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean isDone() {
            return true;
        }

        public Void get() throws InterruptedException, ExecutionException {
            if (ex != null) {
                throw ex;
            }
            return null;
        }

        public Void get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            if (ex != null) {
                throw ex;
            }
            return null;
        }
    }
}
