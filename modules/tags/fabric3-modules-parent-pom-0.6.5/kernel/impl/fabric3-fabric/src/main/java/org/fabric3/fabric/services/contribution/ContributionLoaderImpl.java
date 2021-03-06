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
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.Import;
import org.fabric3.spi.services.contribution.MatchingExportNotFoundException;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Default implementation of the ContributionLoader. Classloaders corresponding to loaded contributions are registered by name with the system
 * ClassLoaderRegistry.
 *
 * @version $Rev$ $Date$
 */
public class ContributionLoaderImpl implements ContributionLoader {
    private static final URI APP_CLASSLOADER = URI.create("fabric3://runtime/ApplicationClassLoader");
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
    }

    public ClassLoader loadContribution(Contribution contribution) throws ContributionLoadException, MatchingExportNotFoundException {
        URI contributionUri = contribution.getUri();
        ClassLoader cl = classLoaderRegistry.getClassLoader(APP_CLASSLOADER);
        if (!classloaderIsolation) {
            // the host environment does not support classloader isolation so only verify extensions are present
            verifyImports(contribution);
            return cl;
        }
        MultiParentClassLoader loader = new MultiParentClassLoader(contributionUri, cl);
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
        resolveImports(contribution, loader);
        // register the classloader
        classLoaderRegistry.register(contributionUri, loader);
        return loader;
    }

    private void resolveImports(Contribution contribution, MultiParentClassLoader loader)
            throws MatchingExportNotFoundException, ContributionLoadException {
        ContributionManifest manifest = contribution.getManifest();
        for (Import imprt : manifest.getImports()) {
            Contribution imported = store.resolve(imprt);
            if (imported == null) {
                String id = imprt.toString();
                throw new MatchingExportNotFoundException("No matching export found for: " + id, id);
            }
            // add the resolved URI to the contribution
            URI importedUri = imported.getUri();
            contribution.addResolvedImportUri(importedUri);
            // add the imported classloader
            ClassLoader importedLoader = classLoaderRegistry.getClassLoader(importedUri);
            if (importedLoader == null) {
                // TODO load in a transient classloader
                String uri = importedUri.toString();
                throw new ContributionLoadException("Imported classloader could not be found: " + uri, uri);
            }
            loader.addParent(importedLoader);
        }
    }

    private void verifyImports(Contribution contribution)
            throws MatchingExportNotFoundException, ContributionLoadException {
        ContributionManifest manifest = contribution.getManifest();
        for (Import imprt : manifest.getImports()) {
            Contribution imported = store.resolve(imprt);
            if (imported == null) {
                String id = imprt.toString();
                throw new MatchingExportNotFoundException("No matching export found for: " + id, id);
            }
            // add the resolved URI to the contribution
            URI importedUri = imported.getUri();
            contribution.addResolvedImportUri(importedUri);
        }
    }

}
