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
 * --- Original Apache License ---
 *
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
package org.fabric3.fabric.runtime.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import javax.management.MBeanServer;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;

import org.fabric3.fabric.instantiator.component.AtomicComponentInstantiator;
import org.fabric3.fabric.instantiator.component.ComponentInstantiator;
import org.fabric3.fabric.services.contribution.manifest.MavenPOMProcessor;
import org.fabric3.fabric.services.documentloader.DocumentLoader;
import org.fabric3.fabric.services.documentloader.DocumentLoaderImpl;
import org.fabric3.fabric.services.synthesizer.SingletonComponentSynthesizer;
import org.fabric3.host.Names;
import static org.fabric3.host.Names.BOOT_CONTRIBUTION;
import static org.fabric3.host.Names.HOST_CONTRIBUTION;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.DefaultValidationContext;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.fabric3.spi.introspection.validation.InvalidContributionException;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.runtime.RuntimeServices;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionState;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.lcm.LogicalComponentManager;
import org.fabric3.spi.services.synthesize.ComponentRegistrationException;
import org.fabric3.spi.services.synthesize.ComponentSynthesizer;
import org.fabric3.spi.xml.XMLFactory;
import org.fabric3.fabric.runtime.bootstrap.BootstrapIntrospectionFactory;
import org.fabric3.system.scdl.SystemImplementation;

/**
 * The base Bootstrapper implementation.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractBootstrapper implements Bootstrapper {

    private static final URI RUNTIME_SERVICES = URI.create("fabric3://RuntimeServices");

    // bootstrap components - these are disposed of after the core runtime system components are booted
    private final ContractProcessor contractProcessor;
    private final ComponentInstantiator instantiator;
    private final ImplementationProcessor<SystemImplementation> systemImplementationProcessor;
    private ComponentSynthesizer synthesizer;

    // runtime components - these are persistent and supplied by the runtime implementation
    private MonitorFactory monitorFactory;
    private ClassLoaderRegistry classLoaderRegistry;
    private MetaDataStore metaDataStore;
    private ScopeRegistry scopeRegistry;
    private LogicalCompositeComponent domain;
    private LogicalComponentManager logicalComponetManager;
    private ComponentManager componentManager;
    private ScopeContainer<?> scopeContainer;

    private XMLFactory xmlFactory;

    private Domain runtimeDomain;

    private Fabric3Runtime<?> runtime;
    private ClassLoader bootClassLoader;
    private List<URL> bootManifests;
    private ClassLoader hostClassLoader;

    protected AbstractBootstrapper(XMLFactory xmlFactory) {
        this.xmlFactory = xmlFactory;
        // create components needed for to bootstrap the runtime
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        contractProcessor = new DefaultContractProcessor(helper);
        DocumentLoader documentLoader = new DocumentLoaderImpl();
        instantiator = new AtomicComponentInstantiator(documentLoader);
        systemImplementationProcessor = BootstrapIntrospectionFactory.createSystemImplementationProcessor();
    }

    public void bootRuntimeDomain(Fabric3Runtime<?> runtime, ClassLoader bootClassLoader, List<URL> bootManifests) throws InitializationException {

        this.runtime = runtime;
        this.bootClassLoader = bootClassLoader;
        this.bootManifests = bootManifests;
        // classloader shared by extension and application classes
        this.hostClassLoader = runtime.getHostClassLoader();

        monitorFactory = runtime.getMonitorFactory();
        HostInfo hostInfo = runtime.getHostInfo();

        RuntimeServices runtimeServices = runtime.getSystemComponent(RuntimeServices.class, RUNTIME_SERVICES);
        logicalComponetManager = runtimeServices.getLogicalComponentManager();
        componentManager = runtimeServices.getComponentManager();
        domain = logicalComponetManager.getRootComponent();
        classLoaderRegistry = runtimeServices.getClassLoaderRegistry();
        metaDataStore = runtimeServices.getMetaDataStore();
        scopeRegistry = runtimeServices.getScopeRegistry();
        scopeContainer = runtimeServices.getScopeContainer();

        synthesizer = new SingletonComponentSynthesizer(systemImplementationProcessor,
                                                        instantiator,
                                                        logicalComponetManager,
                                                        componentManager,
                                                        contractProcessor,
                                                        scopeContainer);

        // register primordial components provided by the runtime itself
        registerRuntimeComponents();

        runtimeDomain = BootstrapAssemblyFactory.createDomain(monitorFactory,
                                                              classLoaderRegistry,
                                                              scopeRegistry,
                                                              componentManager,
                                                              logicalComponetManager,
                                                              metaDataStore,
                                                              runtime.getMBeanServer(),
                                                              runtime.getJMXSubDomain(),
                                                              hostInfo);

        // create and register bootstrap components provided by this bootstrapper
        registerDomain();

        // register the classloaders
        synthesizeContributions();

    }

    public void bootSystem() throws InitializationException {
        try {

            // load the system composite
            Composite composite = loadSystemComposite(BOOT_CONTRIBUTION, bootClassLoader, systemImplementationProcessor, monitorFactory);

            // load user configuration
            Document userConfig = loadUserConfig();
            if (userConfig != null) {
                domain.setPropertyValue("userConfig", userConfig);
            }

            // load system configuration
            Document systemConfig = loadSystemConfig();
            if (systemConfig != null) {
                domain.setPropertyValue("systemConfig", systemConfig);
            }

            // deploy the composite to the runtime domain
            runtimeDomain.include(composite);
        } catch (DeploymentException e) {
            throw new InitializationException(e);
        }

    }

    protected XMLFactory getXmlFactory() {
        return xmlFactory;
    }

    /**
     * Loads the composite that supplies core system components to the runtime.
     *
     * @param contributionUri the synthetic contrbution URI the core components are part of
     * @param bootClassLoader the classloader core components are loaded in
     * @param processor       the ImplementationProcessor for introspecting component implementations.
     * @param monitorFactory  the MonitorFactory for reporting events
     * @return the loaded composite
     * @throws InitializationException if an error occurs loading the composite
     */
    protected abstract Composite loadSystemComposite(URI contributionUri,
                                                     ClassLoader bootClassLoader,
                                                     ImplementationProcessor<SystemImplementation> processor,
                                                     MonitorFactory monitorFactory) throws InitializationException;

    /**
     * Subclasses return a Document representing the domain-level user configuration property or null if none is defined. This property may be
     * referenced entirely or in part via XPath by end-user components in the application domain to supply configuration values.
     *
     * @return a Document representing the domain-level user configuration property or null if none is defined
     * @throws InitializationException if an error occurs loading the configuration file
     */
    protected abstract Document loadUserConfig() throws InitializationException;

    /**
     * Subclasses return a Document representing the domain-level runtime configuration property or null if none is defined. This property may be
     * referenced entirely or in part via XPath by components in the runtime domain to supply configuration values.
     *
     * @return a Document representing the domain-level user configuration property or null if none is defined
     * @throws InitializationException if an error occurs loading the configuration file
     */
    protected abstract Document loadSystemConfig() throws InitializationException;

    /**
     * Registers the primordial runtime components.
     *
     * @throws InitializationException if there is an error during registration
     */
    @SuppressWarnings({"unchecked"})
    private <T extends HostInfo> void registerRuntimeComponents() throws InitializationException {

        // services available through the outward facing Fabric3Runtime API
        registerComponent("MonitorFactory", MonitorFactory.class, monitorFactory, true);
        Class<T> type = (Class<T>) runtime.getHostInfoType();
        T info = (T) runtime.getHostInfo();
        registerComponent("HostInfo", type, info, true);
        MBeanServer mbServer = runtime.getMBeanServer();
        if (mbServer != null) {
            registerComponent("MBeanServer", MBeanServer.class, mbServer, false);
        }

        // services available through the inward facing RuntimeServices SPI
        registerComponent("ComponentManager", ComponentManager.class, componentManager, true);
        registerComponent("RuntimeLogicalComponentManager", LogicalComponentManager.class, logicalComponetManager, true);
        registerComponent("CompositeScopeContainer", ScopeContainer.class, scopeContainer, true);
        registerComponent("ClassLoaderRegistry", ClassLoaderRegistry.class, classLoaderRegistry, true);

        registerComponent("ScopeRegistry", ScopeRegistry.class, scopeRegistry, true);

        registerComponent("MetaDataStore", MetaDataStore.class, metaDataStore, true);
    }

    /**
     * Registers the runtime domain
     *
     * @throws InitializationException if there is an error during registration
     */
    private void registerDomain() throws InitializationException {
        registerComponent("RuntimeDomain", Domain.class, runtimeDomain, true);
        // the following is a hack to initialize the domain
        runtime.getSystemComponent(Domain.class, Names.RUNTIME_DOMAIN_SERVICE_URI);
    }


    /**
     * Registers a primordial component
     *
     * @param name       the component name
     * @param type       the service interface type
     * @param instance   the component instance
     * @param introspect true if the component should be introspected for references
     * @throws InitializationException if there is an error during registration
     */
    private <S, I extends S> void registerComponent(String name, Class<S> type, I instance, boolean introspect) throws InitializationException {
        try {
            synthesizer.registerComponent(name, type, instance, introspect);
        } catch (ComponentRegistrationException e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Creates contributions for the host and boot classloaders. These contributions may be imported by extensions and user contributions.
     *
     * @throws InitializationException if there is an error synthesizing the contributions
     */
    private void synthesizeContributions() throws InitializationException {
        try {
            synthesizeContribution(HOST_CONTRIBUTION, Collections.<URL>emptyList(), hostClassLoader);
            synthesizeContribution(BOOT_CONTRIBUTION, bootManifests, bootClassLoader);
        } catch (ContributionException e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Synthesizes a contribution from a classloader and installs it.
     *
     * @param contributionUri the contribution URI
     * @param manifests       a list of URLs to introspect to create an SCA manifest containing exports for the contribution
     * @param loader          the classloader
     * @throws ContributionException if there is an error synthesizing the contribution
     */
    private void synthesizeContribution(URI contributionUri, List<URL> manifests, ClassLoader loader) throws ContributionException {
        Contribution contribution = new Contribution(contributionUri);
        contribution.setState(ContributionState.INSTALLED);
        ValidationContext context = new DefaultValidationContext();
        XMLInputFactory xmlInputFactory = xmlFactory.newInputFactoryInstance();
        InputStream stream = null;
        XMLStreamReader reader = null;
        for (URL url : manifests) {
            try {
                stream = url.openStream();
                ContributionManifest manifest = contribution.getManifest();
                MavenPOMProcessor processor = new MavenPOMProcessor();
                reader = xmlInputFactory.createXMLStreamReader(stream);
                // advance to first tag
                while (reader.hasNext() && XMLStreamConstants.START_ELEMENT != reader.getEventType()) {
                    reader.next();
                }
                if (XMLStreamConstants.END_DOCUMENT == reader.getEventType()) {
                    return;
                }
                processor.process(manifest, reader, context);
            } catch (IOException e) {
                throw new ContributionException("Invalid manifest: " + url, e);
            } catch (XMLStreamException e) {
                throw new ContributionException("Unable to read manifest: " + url, e);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (context.hasErrors()) {
            throw new InvalidContributionException(context.getErrors(), context.getWarnings());
        }
        metaDataStore.store(contribution);
        classLoaderRegistry.register(contributionUri, loader);
    }


}