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
package org.fabric3.contribution;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.contribution.manifest.ContributionImport;
import static org.fabric3.host.Names.HOST_CONTRIBUTION;
import org.fabric3.host.contribution.ContributionInUseException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.builder.classloader.ClassLoaderWireBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.UnresolvedImportException;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.generator.ClassLoaderWireGenerator;
import org.fabric3.spi.model.physical.PhysicalClassLoaderWireDefinition;

/**
 * Default implementation of the ContributionLoader. Classloaders corresponding to loaded contributions are registered by name with the system
 * ClassLoaderRegistry.
 *
 * @version $Rev$ $Date$
 */
public class ContributionLoaderImpl implements ContributionLoader {
    private final ContributionImport hostImport;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final MetaDataStore store;
    private final ClasspathProcessorRegistry classpathProcessorRegistry;
    private boolean classloaderIsolation;
    private Map<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>> generators;
    private ClassLoaderWireBuilder builder;

    public ContributionLoaderImpl(@Reference ClassLoaderRegistry classLoaderRegistry,
                                  @Reference MetaDataStore store,
                                  @Reference ClasspathProcessorRegistry classpathProcessorRegistry,
                                  @Reference Map<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>> generators,
                                  @Reference ClassLoaderWireBuilder builder,
                                  @Reference HostInfo info) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.store = store;
        this.classpathProcessorRegistry = classpathProcessorRegistry;
        this.generators = generators;
        this.builder = builder;
        classloaderIsolation = info.supportsClassLoaderIsolation();
        hostImport = new ContributionImport(HOST_CONTRIBUTION);
    }

    public ClassLoader load(Contribution contribution) throws ContributionLoadException, UnresolvedImportException {
        URI contributionUri = contribution.getUri();
        ClassLoader hostClassLoader = classLoaderRegistry.getClassLoader(HOST_CONTRIBUTION);
        // all contributions implicitly import the host contribution
        contribution.getManifest().addImport(hostImport);

        // verify and resolve the imports
        List<ContributionWire<?, ?>> wires = resolveImports(contribution);
        if (!classloaderIsolation) {
            // the host environment does not support classloader isolation, return the host classloader
            return hostClassLoader;
        }
        MultiParentClassLoader loader = new MultiParentClassLoader(contributionUri, hostClassLoader);
        List<URL> classpath;
        try {
            // construct the classpath for contained resources in the contribution
            classpath = classpathProcessorRegistry.process(contribution.getLocation());
        } catch (IOException e) {
            throw new ContributionLoadException(e);
        }

        for (URL library : classpath) {
            loader.addURL(library);
        }

        // connect imported contribution classloaders according to their wires
        for (ContributionWire<?, ?> wire : wires) {
            ClassLoaderWireGenerator generator = generators.get(wire.getClass());
            if (generator == null) {
                // not all contribution wires resolve resources through classloaders, so skip if one is not found
                continue;
            }
            PhysicalClassLoaderWireDefinition wireDefinition = generator.generate(wire);
            builder.build(loader, wireDefinition);
        }

        // add contributions that extend extension points provided by this contribution
        List<URI> extenders = resolveExtensionProviders(contribution);
        for (URI uri : extenders) {
            ClassLoader cl = classLoaderRegistry.getClassLoader(uri);
            if (cl == null) {
                // the extension provider may not have been loaded yet
                continue;
            }
            if (!(cl instanceof MultiParentClassLoader)) {
                throw new AssertionError("Extension point provider classloader must be a " + MultiParentClassLoader.class.getName());
            }
            loader.addExtensionClassLoader((MultiParentClassLoader) cl);
        }
        // add this contribution to extension points it extends
        List<URI> extensionPoints = resolveExtensionPoints(contribution);
        for (URI uri : extensionPoints) {
            ClassLoader cl = classLoaderRegistry.getClassLoader(uri);
            if (cl == null) {
                // the extension point may not have been loaded yet
                continue;
            }
            if (!(cl instanceof MultiParentClassLoader)) {
                throw new AssertionError("Extension point classloader must be a " + MultiParentClassLoader.class.getName());
            }
            ((MultiParentClassLoader) cl).addExtensionClassLoader(loader);
        }

        // register the classloader
        classLoaderRegistry.register(contributionUri, loader);
        return loader;
    }

    public void unload(Contribution contribution) throws ContributionInUseException {
        URI uri = contribution.getUri();
        Set<Contribution> contributions = store.resolveDependentContributions(uri);
        if (!contributions.isEmpty()) {
            Set<URI> dependents = new HashSet<URI>(contributions.size());
            for (Contribution dependent : contributions) {
                if (ContributionState.INSTALLED == dependent.getState()) {
                    dependents.add(dependent.getUri());
                }
            }
            if (!dependents.isEmpty()) {
                throw new ContributionInUseException("Contribution is in use: " + uri, uri, dependents);
            }
        }
        classLoaderRegistry.unregister(uri);
    }


    private List<ContributionWire<?, ?>> resolveImports(Contribution contribution) throws UnresolvedImportException {
        // clear the wires as the contribution may have been loaded previously
        contribution.getWires().clear();
        List<ContributionWire<?, ?>> resolved = new ArrayList<ContributionWire<?, ?>>();
        ContributionManifest manifest = contribution.getManifest();
        for (Import imprt : manifest.getImports()) {
            URI uri = contribution.getUri();
            ContributionWire<?, ?> wire = store.resolve(uri, imprt);
            // add the resolved wire to the contribution
            contribution.addWire(wire);
            resolved.add(wire);
        }
        return resolved;
    }

    private List<URI> resolveExtensionProviders(Contribution contribution) {
        List<URI> uris = new ArrayList<URI>();
        ContributionManifest manifest = contribution.getManifest();
        for (String extensionPoint : manifest.getExtensionPoints()) {
            List<Contribution> providers = store.resolveExtensionProviders(extensionPoint);
            for (Contribution provider : providers) {
                uris.add(provider.getUri());
                contribution.addResolvedExtensionProvider(provider.getUri());
            }
        }
        return uris;
    }

    private List<URI> resolveExtensionPoints(Contribution contribution) {
        List<URI> uris = new ArrayList<URI>();
        ContributionManifest manifest = contribution.getManifest();
        for (String extend : manifest.getExtends()) {
            List<Contribution> extensionPoints = store.resolveExtensionPoints(extend);
            for (Contribution extensionPoint : extensionPoints) {
                uris.add(extensionPoint.getUri());
                extensionPoint.addResolvedExtensionProvider(contribution.getUri());
            }
        }
        return uris;
    }
}
