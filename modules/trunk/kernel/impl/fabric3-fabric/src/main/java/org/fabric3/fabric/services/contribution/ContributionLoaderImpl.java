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
package org.fabric3.fabric.services.contribution;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.services.contribution.manifest.ContributionImport;
import static org.fabric3.host.Names.HOST_CONTRIBUTION;
import org.fabric3.host.contribution.ContributionInUseException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionWire;
import org.fabric3.spi.services.contribution.Import;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.UnresolvedImportException;

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
    private Map<Class<? extends ContributionWire<?, ?>>, ContributionWireConnector<?>> connectors;

    public ContributionLoaderImpl(@Reference ClassLoaderRegistry classLoaderRegistry,
                                  @Reference MetaDataStore store,
                                  @Reference ClasspathProcessorRegistry classpathProcessorRegistry,
                                  @Reference Map<Class<? extends ContributionWire<?, ?>>, ContributionWireConnector<?>> connectors,
                                  @Reference HostInfo info) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.store = store;
        this.classpathProcessorRegistry = classpathProcessorRegistry;
        this.connectors = connectors;
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
            ContributionWireConnector connector = connectors.get(wire.getClass());
            assert connector != null;
            URI uri = wire.getExportContributionUri();
            // the classloader name is the same as the contribution URI
            ClassLoader targetClassLoader = classLoaderRegistry.getClassLoader(uri);
            connector.connect(wire, loader, targetClassLoader);
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
                dependents.add(dependent.getUri());
            }
            throw new ContributionInUseException("Contribution is in use: " + uri, uri, dependents);
        }
        classLoaderRegistry.unregister(uri);
    }


    private List<ContributionWire<?, ?>> resolveImports(Contribution contribution) throws UnresolvedImportException {
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


}
