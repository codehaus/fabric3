/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.contribution;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import org.osoa.sca.annotations.Reference;

import org.fabric3.contribution.wire.ContributionWireInstantiatorRegistry;
import org.fabric3.host.Names;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.StoreException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.contribution.UnresolvedImportException;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Default MetaDataStore implementation
 *
 * @version $Rev$ $Date$
 */
public class MetaDataStoreImpl implements MetaDataStore {
    public static final QName COMPOSITE = new QName(Constants.SCA_NS, "composite");
    private Map<URI, Contribution> cache = new ConcurrentHashMap<URI, Contribution>();
    private Map<QName, Map<Export, Contribution>> exportsToContributionCache = new ConcurrentHashMap<QName, Map<Export, Contribution>>();
    private ProcessorRegistry processorRegistry;
    private ClassLoaderRegistry classLoaderRegistry;
    private ContributionWireInstantiatorRegistry instantiatorRegistry;

    public MetaDataStoreImpl(ClassLoaderRegistry classLoaderRegistry, ProcessorRegistry processorRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.processorRegistry = processorRegistry;
    }

    /**
     * Used to reinject the processor registry after runtime bootstrap.
     *
     * @param processorRegistry the configured processor registry
     */
    @Reference
    public void setProcessorRegistry(ProcessorRegistry processorRegistry) {
        this.processorRegistry = processorRegistry;
    }

    @Reference
    public void setInstantiatorRegistry(ContributionWireInstantiatorRegistry instantiatorRegistry) {
        this.instantiatorRegistry = instantiatorRegistry;
    }

    public void store(Contribution contribution) throws StoreException {
        cache.put(contribution.getUri(), contribution);
        addToExports(contribution);
    }

    public Contribution find(URI contributionUri) {
        return cache.get(contributionUri);
    }

    public Set<Contribution> getContributions() {
        return new HashSet<Contribution>(cache.values());
    }

    public void remove(URI contributionUri) {
        Contribution contribution = find(contributionUri);
        if (contribution != null) {
            List<Export> exports = contribution.getManifest().getExports();
            for (Export export : exports) {
                Map<Export, Contribution> types = exportsToContributionCache.get(export.getType());
                if (types == null) {
                    // programming error
                    throw new AssertionError("Export type not found: " + export.getType());
                }
                types.remove(export);
            }
        }
        cache.remove(contributionUri);
    }

    @SuppressWarnings({"unchecked"})
    public <S extends Symbol> ResourceElement<S, ?> resolve(S symbol) throws StoreException {
        for (Contribution contribution : cache.values()) {
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                    if (element.getSymbol().equals(symbol)) {
                        if (!resource.isProcessed()) {
                            // this is a programming error as resolve(Symbol) should only be called after contribution resources have been processed
                            throw new StoreException("Attempt to resolve a resource before it is processed");
                        }
                        return (ResourceElement<S, ?>) element;
                    }
                }
            }
        }
        return null;
    }

    public Contribution resolveContainingContribution(Symbol symbol) {
        for (Contribution contribution : cache.values()) {
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                    if (element.getSymbol().equals(symbol)) {
                        return contribution;
                    }
                }
            }
        }
        return null;
    }

    public Resource resolveContainingResource(URI contributionUri, Symbol symbol) {
        Contribution contribution = cache.get(contributionUri);
        if (contribution != null) {
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                    if (element.getSymbol().equals(symbol)) {
                        return resource;
                    }
                }
            }
        }
        return null;
    }

    public <S extends Symbol, V extends Serializable> ResourceElement<S, V> resolve(URI contributionUri,
                                                                                    Class<V> type,
                                                                                    S symbol,
                                                                                    IntrospectionContext context)
            throws StoreException {
        Contribution contribution = find(contributionUri);
        if (contribution == null) {
            String identifier = contributionUri.toString();
            throw new ContributionResolutionException("Contribution not found: " + identifier, identifier);
        }
        ResourceElement<S, V> element = resolveInternal(contribution, type, symbol, context);
        if (element != null) {
            return element;
        }
        for (ContributionWire<?, ?> wire : contribution.getWires()) {
            if (!wire.resolves(symbol)) {
                // the wire doesn't resolve the specific resource
                continue;
            }
            URI uri = wire.getExportContributionUri();

            Contribution resolved = cache.get(uri);
            if (resolved == null) {
                String identifier = contributionUri.toString();
                throw new ContributionResolutionException("Dependent contibution not found: " + identifier, identifier);
            }
            element = resolveInternal(resolved, type, symbol, context);
            if (element != null) {
                return element;
            }
        }
        return null;
    }

    public Contribution resolve(Import imprt) {
        Map<Export, Contribution> exports = exportsToContributionCache.get(imprt.getType());
        if (exports != null) {
            for (Map.Entry<Export, Contribution> entry : exports.entrySet()) {
                Export export = entry.getKey();
                if (Export.EXACT_MATCH == export.match(imprt)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public ContributionWire<?, ?> resolve(URI uri, Import imprt) throws UnresolvedImportException {
        Map<Export, Contribution> map = exportsToContributionCache.get(imprt.getType());
        if (map == null) {
            return null;
        }
        for (Map.Entry<Export, Contribution> entry : map.entrySet()) {
            Export export = entry.getKey();
            int level = export.match(imprt);
            if (level == Export.EXACT_MATCH) {
                if (instantiatorRegistry == null) {
                    // Programming error: an illegal attempt to resolve a contribution before bootstrap has completed. 
                    throw new AssertionError("Instantiator not yet configured");
                }
                URI exportUri = entry.getValue().getUri();
                return instantiatorRegistry.instantiate(imprt, export, uri, exportUri);
            }
        }
        throw new UnresolvedImportException("Unable to resolve import: " + imprt);
    }

    public Set<Contribution> resolveDependentContributions(URI uri) {
        Set<Contribution> dependents = new HashSet<Contribution>();
        for (Contribution entry : cache.values()) {
            List<ContributionWire<?, ?>> contributionWires = entry.getWires();
            for (ContributionWire<?, ?> wire : contributionWires) {
                if (uri.equals(wire.getExportContributionUri())) {
                    dependents.add(entry);
                    break;
                }
            }
        }
        return dependents;
    }

    public List<Contribution> resolveExtensionProviders(String name) {
        List<Contribution> providers = new ArrayList<Contribution>();
        for (Contribution contribution : cache.values()) {
            for (String extend : contribution.getManifest().getExtends()) {
                if (extend.equals(name)) {
                    providers.add(contribution);
                    break;
                }
            }
        }
        return providers;
    }

    public List<Contribution> resolveExtensionPoints(String name) {
        List<Contribution> extensionPoints = new ArrayList<Contribution>();
        for (Contribution contribution : cache.values()) {
            for (String extensionPoint : contribution.getManifest().getExtensionPoints()) {
                if (extensionPoint.equals(name)) {
                    extensionPoints.add(contribution);
                    break;
                }
            }
        }
        return extensionPoints;
    }

    public Set<Contribution> resolveCapabilities(Contribution contribution) {
        Set<Contribution> extensions = new HashSet<Contribution>();
        return resolveCapabilities(contribution, extensions);
    }

    public Set<Contribution> resolveCapability(String capability) {
        Set<Contribution> extensions = new HashSet<Contribution>();
        for (Contribution entry : cache.values()) {
            if (entry.getManifest().getProvidedCapabilities().contains(capability) && !extensions.contains(entry)) {
                extensions.add(entry);
                resolveCapabilities(entry, extensions);
            }
        }
        return extensions;
    }

    public List<Resource> resolveResources(URI contributionUri) {
        List<Resource> resources = new ArrayList<Resource>();
        Contribution contribution = cache.get(contributionUri);
        Set<URI> visited = new HashSet<URI>();
        resolveResources(contribution, resources, visited);
        return resources;
    }

    public List<Resource> resolveResources(Contribution contribution, List<Resource> resources, Set<URI> visited) {
        resources.addAll(contribution.getResources());
        visited.add(contribution.getUri());
        for (ContributionWire<?, ?> wire : contribution.getWires()) {
            Contribution imported = cache.get(wire.getExportContributionUri());
            if (!visited.contains(imported.getUri())
                    && !imported.getUri().equals(Names.HOST_CONTRIBUTION)
                    && !imported.getUri().equals(Names.BOOT_CONTRIBUTION)) {
                // recurse for the imported contribution
                resolveResources(imported, resources, visited);
            }
        }
        return resources;
    }

    private Set<Contribution> resolveCapabilities(Contribution contribution, Set<Contribution> extensions) {
        Set<String> required = contribution.getManifest().getRequiredCapabilities();
        for (String capability : required) {
            for (Contribution entry : cache.values()) {
                if (entry.getManifest().getProvidedCapabilities().contains(capability) && !extensions.contains(entry)) {
                    extensions.add(entry);
                    resolveCapabilities(entry, extensions);
                }
            }
        }
        for (ContributionWire<?, ?> wire : contribution.getWires()) {
            Contribution imported = cache.get(wire.getExportContributionUri());
            if (!extensions.contains(imported)
                    && !imported.getUri().equals(Names.HOST_CONTRIBUTION)
                    && !imported.getUri().equals(Names.BOOT_CONTRIBUTION)) {
                extensions.add(imported);
            }
            // recurse for the imported contribution
            resolveCapabilities(imported, extensions);
        }
        for (URI uri : contribution.getResolvedExtensionProviders()) {
            Contribution provider = cache.get(uri);
            if (!extensions.contains(provider)) {
                extensions.add(provider);
            }
            // TODO figure out how to recurse up providers without introducing a cycle
//            resolveCapabilities(provider, extensions);
        }
        return extensions;
    }


    @SuppressWarnings({"unchecked"})
    private <S extends Symbol, V extends Serializable> ResourceElement<S, V> resolveInternal(Contribution contribution,
                                                                                             Class<V> type,
                                                                                             S symbol,
                                                                                             IntrospectionContext context)
            throws StoreException {
        URI contributionUri = contribution.getUri();
        ClassLoader loader = classLoaderRegistry.getClassLoader(contributionUri);
        assert loader != null;
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (element.getSymbol().equals(symbol)) {
                    if (!resource.isProcessed()) {
                        try {
                            processorRegistry.processResource(contributionUri, resource, context, loader);
                        } catch (ContributionException e) {
                            String identifier = resource.getUrl().toString();
                            throw new StoreException("Error resolving resource: " + identifier, identifier, e);
                        }
                    }
                    Object val = element.getValue();
                    if (!type.isInstance(val)) {
                        throw new IllegalArgumentException("Invalid type for symbol. Expected: " + type + " was: " + val);
                    }
                    return (ResourceElement<S, V>) element;
                }
            }
        }
        return null;
    }

    /**
     * Adds the contribution exports to the cached list of exports for the domain
     *
     * @param contribution the contribution containing the exports to add
     */
    private void addToExports(Contribution contribution) {

        List<Export> exports = contribution.getManifest().getExports();
        if (exports.size() > 0) {
            for (Export export : exports) {
                Map<Export, Contribution> map = exportsToContributionCache.get(export.getType());
                if (map == null) {
                    map = new ConcurrentHashMap<Export, Contribution>();
                    exportsToContributionCache.put(export.getType(), map);
                }
                map.put(export, contribution);
            }
        }
    }

}
