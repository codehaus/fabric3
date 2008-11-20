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

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.xml.namespace.QName;

import org.fabric3.fabric.services.contribution.manifest.XmlManifestProcessor;
import static org.fabric3.host.Names.APPLICATION_DOMAIN_URI;
import static org.fabric3.host.Names.BOOT_CLASSLOADER_ID;
import static org.fabric3.host.Names.CONTRIBUTION_SERVICE_URI;
import static org.fabric3.host.Names.RUNTIME_DOMAIN_SERVICE_URI;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.StoreException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.runtime.StartException;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.DefaultValidationContext;
import org.fabric3.scdl.Include;
import org.fabric3.scdl.ValidationContext;
import static org.fabric3.spi.Namespaces.IMPLEMENTATION;
import static org.fabric3.spi.SPINames.DEFINITIONS_REGISTRY;
import static org.fabric3.spi.SPINames.EVENT_SERVICE_URI;
import static org.fabric3.spi.SPINames.METADATA_STORE_URI;
import static org.fabric3.spi.SPINames.XML_MANIFEST_PROCESSOR_URI;
import org.fabric3.spi.introspection.validation.InvalidContributionException;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionState;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.definitions.DefinitionActivationException;
import org.fabric3.spi.services.definitions.DefinitionsRegistry;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.services.event.JoinDomain;
import org.fabric3.spi.services.event.Recover;
import org.fabric3.spi.services.event.RuntimeStart;

/**
 * Default implementation of a RuntimeLifecycleCoordinator.
 *
 * @version $Rev$ $Date$
 */
public class DefaultCoordinator<RUNTIME extends Fabric3Runtime<?>, BOOTSTRAPPER extends Bootstrapper>
        implements RuntimeLifecycleCoordinator<RUNTIME, BOOTSTRAPPER> {
    private State state = State.UNINITIALIZED;
    private RUNTIME runtime;
    private BOOTSTRAPPER bootstrapper;
    private ClassLoader bootClassLoader;
    private List<URL> bootExports;
    private ContributionSource intents;
    private List<ContributionSource> extensions;
    private List<ContributionSource> userExtensions;

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

    public void setConfiguration(BootConfiguration<RUNTIME, BOOTSTRAPPER> configuration) {
        runtime = configuration.getRuntime();
        bootstrapper = configuration.getBootstrapper();
        bootClassLoader = configuration.getBootClassLoader();
        bootExports = configuration.getBootLibraryExports();
        intents = configuration.getIntents();
        extensions = configuration.getExtensions();
        userExtensions = configuration.getUserExtensions();
    }

    public void bootPrimordial() throws InitializationException {
        if (state != State.UNINITIALIZED) {
            throw new IllegalStateException("Not in UNINITIALIZED state");
        }
        runtime.initialize();
        bootstrapper.bootRuntimeDomain(runtime, bootClassLoader);
        state = State.PRIMORDIAL;
    }


    public void initialize() throws InitializationException {

        if (state != State.PRIMORDIAL) {
            throw new IllegalStateException("Not in PRIMORDIAL state");
        }
        // initialize core system components
        bootstrapper.bootSystem();

        synthesizeBootContribution();

        try {
            activateIntents(intents);
            includeExtensions(extensions);
            includeExtensions(userExtensions);
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
        eventService.publish(new Recover());
        state = State.RECOVERED;
        return new SyncFuture();
    }

    public Future<Void> joinDomain(final long timeout) throws InitializationException {
        if (state != State.RECOVERED) {
            throw new IllegalStateException("Not in RECOVERED state");
        }
        EventService eventService = runtime.getSystemComponent(EventService.class, EVENT_SERVICE_URI);
        eventService.publish(new JoinDomain());
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

    protected void activateIntents(ContributionSource source) throws InitializationException {
        try {
            ContributionService contributionService = runtime.getSystemComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
            URI uri = contributionService.contribute(source);
            DefinitionsRegistry definitionsRegistry = runtime.getSystemComponent(DefinitionsRegistry.class, DEFINITIONS_REGISTRY);
            List<URI> intents = new ArrayList<URI>();
            intents.add(uri);
            definitionsRegistry.activateDefinitions(intents);
        } catch (ContributionException e) {
            throw new InitializationException(e);
        } catch (DefinitionActivationException e) {
            throw new InitializationException(e);
        }
    }

    protected void synthesizeBootContribution() throws InitializationException {
        try {
            assert !bootExports.isEmpty();
            XmlManifestProcessor processor =
                    runtime.getSystemComponent(XmlManifestProcessor.class, XML_MANIFEST_PROCESSOR_URI);
            Contribution contribution = new Contribution(BOOT_CLASSLOADER_ID);
            contribution.setState(ContributionState.INSTALLED);
            ValidationContext context = new DefaultValidationContext();
            for (URL url : bootExports) {
                InputStream stream;
                try {
                    stream = url.openStream();
                } catch (IOException e) {
                    throw new InitializationException("boot jar is missing a pom.xml: " + url);
                }
                ContributionManifest manifest = contribution.getManifest();
                processor.process(manifest, stream, context);
            }
            if (context.hasErrors()) {
                throw new InvalidContributionException(context.getErrors(), context.getWarnings());
            }
            MetaDataStore store = runtime.getSystemComponent(MetaDataStore.class, METADATA_STORE_URI);
            store.store(contribution);
        } catch (StoreException e) {
            throw new InitializationException(e);
        } catch (ContributionException e) {
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
        Composite composite = createExtensionComposite(contributionUris);
        try {
            domain.include(composite);
        } catch (DeploymentException e) {
            throw new ExtensionInitializationException("Error activating extensions", e);
        }
    }

    /**
     * Creates an extension composite by including deployables from contributions identified by the list of URIs
     *
     * @param contributionUris the contributions containing the deployables to include
     * @return the extension composite
     * @throws org.fabric3.host.runtime.InitializationException
     *          if an error occurs creating the composite
     */
    protected Composite createExtensionComposite(List<URI> contributionUris) throws InitializationException {
        MetaDataStore metaDataStore = runtime.getSystemComponent(MetaDataStore.class, METADATA_STORE_URI);
        if (metaDataStore == null) {
            String id = METADATA_STORE_URI.toString();
            throw new InitializationException("Extensions metadata store not configured: " + id, id);
        }
        QName qName = new QName(IMPLEMENTATION, "extension");
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