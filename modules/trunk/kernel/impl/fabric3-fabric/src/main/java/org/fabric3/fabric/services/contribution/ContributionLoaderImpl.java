package org.fabric3.fabric.services.contribution;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.osoa.sca.annotations.Reference;

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
    private static final URI APP_CLASSLOADER = URI.create("sca://./applicationClassLoader");
    private final ClassLoaderRegistry classLoaderRegistry;
    private final MetaDataStore store;
    private final ClasspathProcessorRegistry classpathProcessorRegistry;

    public ContributionLoaderImpl(@Reference ClassLoaderRegistry classLoaderRegistry,
                                  @Reference MetaDataStore store,
                                  @Reference ClasspathProcessorRegistry classpathProcessorRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.store = store;
        this.classpathProcessorRegistry = classpathProcessorRegistry;
    }

    public ClassLoader loadContribution(Contribution contribution) throws ContributionLoadException, MatchingExportNotFoundException {
        ClassLoader cl = classLoaderRegistry.getClassLoader(APP_CLASSLOADER);
        URI contributionUri = contribution.getUri();
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
        resolveExtensionImports(contribution, loader);

        // register the classloader 
        classLoaderRegistry.register(contributionUri, loader);
        return loader;
    }

    private ContributionManifest resolveImports(Contribution contribution, MultiParentClassLoader loader)
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
        return manifest;
    }

    private void resolveExtensionImports(Contribution contribution, MultiParentClassLoader loader)
            throws MatchingExportNotFoundException, ContributionLoadException {
        ContributionManifest manifest = contribution.getManifest();
        for (Import imprt : manifest.getExtensionImports()) {
            Contribution imported = store.resolve(imprt);
            if (imported == null) {
                String id = imprt.toString();
                throw new MatchingExportNotFoundException("No matching extension found for: " + id, id);
            }
            // add the resolved extension URI to the contribution
            URI importedUri = imported.getUri();
            contribution.addResolvedExtensionImportUri(importedUri);
            // add the imported classloader
            ClassLoader importedLoader = classLoaderRegistry.getClassLoader(importedUri);
            if (importedLoader == null) {
                String uri = importedUri.toString();
                throw new ContributionLoadException("Imported extension classloader could not be found: " + uri, uri);
            }
            loader.addParent(importedLoader);
        }
    }

}
