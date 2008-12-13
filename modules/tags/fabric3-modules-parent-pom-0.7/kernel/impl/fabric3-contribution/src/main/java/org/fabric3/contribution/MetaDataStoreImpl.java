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

import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

import org.osoa.sca.Constants;
import org.osoa.sca.annotations.Reference;

import org.fabric3.contribution.wire.ContributionWireInstantiatorRegistry;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.StoreException;
import org.fabric3.model.type.ValidationContext;
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
                exportsToContributionCache.remove(export.getType());
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
                                                                                    ValidationContext context)
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

    public boolean isResolved(Import imprt) {
        Map<Export, Contribution> exports = exportsToContributionCache.get(imprt.getType());
        if (exports != null) {
            for (Export export : exports.keySet()) {
                if (Export.EXACT_MATCH == export.match(imprt)) {
                    return true;
                }
            }
        }
        return false;
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

    @SuppressWarnings({"unchecked"})
    private <S extends Symbol, V extends Serializable> ResourceElement<S, V> resolveInternal(Contribution contribution,
                                                                                             Class<V> type,
                                                                                             S symbol,
                                                                                             ValidationContext context)
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
