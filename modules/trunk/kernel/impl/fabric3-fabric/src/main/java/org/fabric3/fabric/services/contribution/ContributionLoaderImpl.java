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
package org.fabric3.fabric.services.contribution;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.services.contribution.manifest.ContributionImport;
import static org.fabric3.host.Names.HOST_CLASSLOADER_ID;
import org.fabric3.host.contribution.ContributionInUseException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.Import;
import org.fabric3.spi.services.contribution.MetaDataStore;

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

    public ContributionLoaderImpl(@Reference ClassLoaderRegistry classLoaderRegistry,
                                  @Reference MetaDataStore store,
                                  @Reference ClasspathProcessorRegistry classpathProcessorRegistry,
                                  @Reference HostInfo info) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.store = store;
        this.classpathProcessorRegistry = classpathProcessorRegistry;
        classloaderIsolation = info.supportsClassLoaderIsolation();
        hostImport = new ContributionImport(HOST_CLASSLOADER_ID);
    }

    public ClassLoader load(Contribution contribution) throws ContributionLoadException {
        URI contributionUri = contribution.getUri();
        ClassLoader hostClassLoader = classLoaderRegistry.getClassLoader(HOST_CLASSLOADER_ID);
        // all contributions implicitly import the host contribution
        contribution.getManifest().addImport(hostImport);

        // verify and resolve the imports
        List<ClassLoader> resolved = resolveImports(contribution);
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
        for (ClassLoader classLoader : resolved) {
            if (classLoader != hostClassLoader) {
                // skip the host classloader as it is the primary parent
                loader.addParent(classLoader);
            }
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


    private List<ClassLoader> resolveImports(Contribution contribution) throws ContributionLoadException {
        List<ClassLoader> resolved = new ArrayList<ClassLoader>();
        ContributionManifest manifest = contribution.getManifest();
        for (Import imprt : manifest.getImports()) {
            Contribution imported = store.resolve(imprt);
            if (imported == null) {
                String id = imprt.toString();
                throw new MatchingExportNotFoundException("No matching export found for: " + id);
            }
            // add the resolved URI to the contribution
            URI importedUri = imported.getUri();
            contribution.addResolvedImportUri(importedUri);
            // add the imported classloader
            ClassLoader importedLoader = classLoaderRegistry.getClassLoader(importedUri);
            assert importedLoader != null;
            resolved.add(importedLoader);
        }
        return resolved;
    }


}
