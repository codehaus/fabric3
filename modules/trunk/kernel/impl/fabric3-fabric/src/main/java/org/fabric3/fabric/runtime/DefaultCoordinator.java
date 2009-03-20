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
package org.fabric3.fabric.runtime;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.fabric3.fabric.runtime.FabricNames.DEFINITIONS_REGISTRY;
import static org.fabric3.fabric.runtime.FabricNames.EVENT_SERVICE_URI;
import static org.fabric3.host.Names.APPLICATION_DOMAIN_URI;
import static org.fabric3.host.Names.COMPOSITE_SYNTHESIZER_URI;
import static org.fabric3.host.Names.CONTRIBUTION_SERVICE_URI;
import static org.fabric3.host.Names.RUNTIME_DOMAIN_SERVICE_URI;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.runtime.StartException;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.services.definitions.DefinitionActivationException;
import org.fabric3.spi.services.definitions.DefinitionsRegistry;
import org.fabric3.spi.services.event.DomainRecover;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.services.event.JoinDomain;
import org.fabric3.spi.services.event.RuntimeRecover;
import org.fabric3.spi.services.event.RuntimeStart;
import org.fabric3.spi.synthesize.CompositeSynthesizer;

/**
 * Default implementation of a RuntimeLifecycleCoordinator.
 *
 * @version $Rev$ $Date$
 */
public class DefaultCoordinator implements RuntimeLifecycleCoordinator {
    private State state = State.UNINITIALIZED;
    private Fabric3Runtime<?> runtime;
    private Bootstrapper bootstrapper;
    private ClassLoader bootClassLoader;
    private ContributionSource intents;
    private List<ContributionSource> extensions;
    private List<ContributionSource> policies;
    private Map<String, String> exportedPackages;

    public enum State {
        UNINITIALIZED,
        PRIMORDIAL,
        INITIALIZED,
        DOMAIN_JOINED,
        RECOVERED,
        STARTED,
        SHUTDOWN,
        ERROR
    }

    public void setConfiguration(BootConfiguration configuration) {
        runtime = configuration.getRuntime();
        bootstrapper = configuration.getBootstrapper();
        bootClassLoader = configuration.getBootClassLoader();
        exportedPackages = configuration.getExportedPackages();
        intents = configuration.getIntents();
        extensions = configuration.getExtensions();
        policies = configuration.getPolicies();
    }

    public void bootPrimordial() throws InitializationException {
        if (state != State.UNINITIALIZED) {
            throw new IllegalStateException("Not in UNINITIALIZED state");
        }
        runtime.initialize();
        bootstrapper.bootRuntimeDomain(runtime, bootClassLoader, exportedPackages);
        state = State.PRIMORDIAL;
    }


    public void initialize() throws InitializationException {

        if (state != State.PRIMORDIAL) {
            throw new IllegalStateException("Not in PRIMORDIAL state");
        }
        // initialize core system components
        bootstrapper.bootSystem();

        try {
            activateDefinitions(intents);
            for (ContributionSource policy : policies) {
                activateDefinitions(policy);
            }
            includeExtensions(extensions);
        } catch (DefinitionActivationException e) {
            throw new InitializationException(e);
        }

        state = State.INITIALIZED;

    }

    public Future<Void> recover() {
        if (state != State.INITIALIZED) {
            throw new IllegalStateException("Not in INITIALIZED state");
        }
        Domain domain = runtime.getSystemComponent(Domain.class, APPLICATION_DOMAIN_URI);
        if (domain == null) {
            String name = APPLICATION_DOMAIN_URI.toString();
            InitializationException e = new InitializationException("Domain not found: " + name, name);
            return new SyncFuture(new ExecutionException(e));

        }
        EventService eventService = runtime.getSystemComponent(EventService.class, EVENT_SERVICE_URI);
        eventService.publish(new RuntimeRecover());
        state = State.RECOVERED;
        return new SyncFuture();
    }

    public Future<Void> joinDomain(final long timeout) throws InitializationException {
        if (state != State.RECOVERED) {
            throw new IllegalStateException("Not in RECOVERED state");
        }
        EventService eventService = runtime.getSystemComponent(EventService.class, EVENT_SERVICE_URI);
        eventService.publish(new JoinDomain());
        eventService.publish(new DomainRecover());
        state = State.DOMAIN_JOINED;
        // no domain to join
        return new SyncFuture();
    }

    public Future<Void> start() {
        if (state != State.DOMAIN_JOINED) {
            throw new IllegalStateException("Not in DOMAIN_JOINED state");
        }
        try {
            runtime.start();
            // starts the runtime by publishing a start event
            EventService eventService = runtime.getSystemComponent(EventService.class, EVENT_SERVICE_URI);
            eventService.publish(new RuntimeStart());
            state = State.STARTED;
        } catch (StartException e) {
            state = State.ERROR;
            return new SyncFuture(new ExecutionException(e));
        }
        return new SyncFuture();
    }

    public Future<Void> shutdown() throws ShutdownException {
        if (state == State.STARTED) {
            runtime.destroy();
            state = State.SHUTDOWN;
        }
        return new SyncFuture();
    }

    protected void activateDefinitions(ContributionSource source) throws InitializationException {
        try {
            ContributionService contributionService = runtime.getSystemComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
            URI uri = contributionService.contribute(source);
            DefinitionsRegistry definitionsRegistry = runtime.getSystemComponent(DefinitionsRegistry.class, DEFINITIONS_REGISTRY);
            List<URI> definitions = new ArrayList<URI>();
            definitions.add(uri);
            definitionsRegistry.activateDefinitions(definitions);
        } catch (ContributionException e) {
            throw new InitializationException(e);
        } catch (DefinitionActivationException e) {
            throw new InitializationException(e);
        }
    }

    protected void includeExtensions(List<ContributionSource> sources) throws InitializationException, DefinitionActivationException {
        try {
            ContributionService contributionService =
                    runtime.getSystemComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
            List<URI> contributionUris = contributionService.contribute(sources);
            includeExtensionContributions(contributionUris);
            DefinitionsRegistry definitionsRegistry =
                    runtime.getSystemComponent(DefinitionsRegistry.class, DEFINITIONS_REGISTRY);
            definitionsRegistry.activateDefinitions(contributionUris);
        } catch (ContributionException e) {
            throw new ExtensionInitializationException("Error contributing extensions", e);
        }
    }

    protected void includeExtensionContributions(List<URI> contributionUris) throws InitializationException {
        Domain domain = runtime.getSystemComponent(Domain.class, RUNTIME_DOMAIN_SERVICE_URI);
        CompositeSynthesizer synthesizer = runtime.getSystemComponent(CompositeSynthesizer.class, COMPOSITE_SYNTHESIZER_URI);
        Composite composite = synthesizer.createComposite(contributionUris);
        try {
            domain.include(composite);
        } catch (DeploymentException e) {
            throw new ExtensionInitializationException("Error activating extensions", e);
        }
    }

    protected static class SyncFuture implements Future<Void> {
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