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
package org.fabric3.fabric.runtime;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.fabric3.fabric.runtime.FabricNames.POLICY_REGISTRY;
import static org.fabric3.fabric.runtime.FabricNames.EVENT_SERVICE_URI;
import static org.fabric3.host.Names.APPLICATION_DOMAIN_URI;
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
import org.fabric3.spi.policy.PolicyActivationException;
import org.fabric3.spi.policy.PolicyRegistry;
import org.fabric3.spi.services.event.DomainRecover;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.services.event.JoinDomain;
import org.fabric3.spi.services.event.RuntimeRecover;
import org.fabric3.spi.services.event.RuntimeStart;

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
    private List<ContributionSource> policies;
    private Map<String, String> exportedPackages;
    private List<ContributionSource> extensionContributions;
    private List<ContributionSource> userContributions;

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
        extensionContributions = configuration.getExtensionContributions();
        userContributions = configuration.getUserContributions();
        policies = configuration.getPolicies();
    }

    public void bootPrimordial() throws InitializationException {
        if (state != State.UNINITIALIZED) {
            throw new IllegalStateException("Not in UNINITIALIZED state");
        }
        runtime.boot();
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
            // install extensions
            List<URI> uris = installContributions(extensionContributions);
            // deploy extensions
            deploy(uris);
        } catch (PolicyActivationException e) {
            throw new InitializationException(e);
        }

        state = State.INITIALIZED;
    }

    public void recover() throws InitializationException {
        if (state != State.INITIALIZED) {
            throw new IllegalStateException("Not in INITIALIZED state");
        }
        Domain domain = runtime.getSystemComponent(Domain.class, APPLICATION_DOMAIN_URI);
        if (domain == null) {
            String name = APPLICATION_DOMAIN_URI.toString();
            throw new InitializationException("Domain not found: " + name, name);
        }
        // install user contibutions - they will be deployed when the domain recovers
        installContributions(userContributions);
        EventService eventService = runtime.getSystemComponent(EventService.class, EVENT_SERVICE_URI);
        eventService.publish(new RuntimeRecover());
        state = State.RECOVERED;
    }

    public void joinDomain(final long timeout) {
        if (state != State.RECOVERED) {
            throw new IllegalStateException("Not in RECOVERED state");
        }
        EventService eventService = runtime.getSystemComponent(EventService.class, EVENT_SERVICE_URI);
        eventService.publish(new JoinDomain());
        eventService.publish(new DomainRecover());
        state = State.DOMAIN_JOINED;
    }

    public void start() throws InitializationException {
        if (state != State.DOMAIN_JOINED) {
            throw new IllegalStateException("Not in DOMAIN_JOINED state");
        }
        // starts the runtime by publishing a start event
        EventService eventService = runtime.getSystemComponent(EventService.class, EVENT_SERVICE_URI);
        eventService.publish(new RuntimeStart());
        state = State.STARTED;
    }

    public void shutdown() throws ShutdownException {
        if (state == State.STARTED) {
            runtime.destroy();
            state = State.SHUTDOWN;
        }
    }

    private void activateDefinitions(ContributionSource source) throws InitializationException {
        try {
            ContributionService contributionService = runtime.getSystemComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
            URI uri = contributionService.contribute(source);
            PolicyRegistry policyRegistry = runtime.getSystemComponent(PolicyRegistry.class, POLICY_REGISTRY);
            List<URI> definitions = new ArrayList<URI>();
            definitions.add(uri);
            policyRegistry.activateDefinitions(definitions);
        } catch (ContributionException e) {
            throw new InitializationException(e);
        } catch (PolicyActivationException e) {
            throw new InitializationException(e);
        }
    }

    private List<URI> installContributions(List<ContributionSource> sources) throws InitializationException {
        try {
            ContributionService contributionService = runtime.getSystemComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
            // install the contributions
            return contributionService.contribute(sources);

        } catch (ContributionException e) {
            throw new ExtensionInitializationException("Error contributing extensions", e);
        }
    }

    private void deploy(List<URI> contributionUris) throws InitializationException, PolicyActivationException {
        try {
            Domain domain = runtime.getSystemComponent(Domain.class, RUNTIME_DOMAIN_SERVICE_URI);
            domain.include(contributionUris, false);
            PolicyRegistry policyRegistry = runtime.getSystemComponent(PolicyRegistry.class, POLICY_REGISTRY);
            policyRegistry.activateDefinitions(contributionUris);
        } catch (DeploymentException e) {
            throw new ExtensionInitializationException("Error deploying extensions", e);
        }
    }

}