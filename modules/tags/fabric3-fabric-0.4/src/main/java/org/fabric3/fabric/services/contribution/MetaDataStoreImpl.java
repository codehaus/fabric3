/*
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
package org.fabric3.fabric.services.contribution;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

import org.osoa.sca.Constants;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.MetaDataStoreException;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.contribution.Symbol;

/**
 * Default IndexStore implementation
 *
 * @version $Rev$ $Date$
 */
public class MetaDataStoreImpl implements MetaDataStore {
    public static final QName COMPOSITE = new QName(Constants.SCA_NS, "composite");
    private Map<URI, Contribution> cache = new ConcurrentHashMap<URI, Contribution>();
    private Map<QName, Map<Export, Contribution>> exportsToContributionCache =
            new ConcurrentHashMap<QName, Map<Export, Contribution>>();
    private ProcessorRegistry processorRegistry;
    private ClassLoaderRegistry classLoaderRegistry;

    public MetaDataStoreImpl(@Reference ClassLoaderRegistry classLoaderRegistry,
                             @Reference ProcessorRegistry processorRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.processorRegistry = processorRegistry;
    }

    public void store(Contribution contribution) throws MetaDataStoreException {
        cache.put(contribution.getUri(), contribution);
        addToExports(contribution);
    }


    public Contribution find(URI contributionUri) {
        return cache.get(contributionUri);
    }

    @SuppressWarnings({"unchecked"})
    public <S extends Symbol> ResourceElement<S, ?> resolve(S symbol) {
        for (Contribution contribution : cache.values()) {
            URI contributionUri = contribution.getUri();
            ClassLoader loader = classLoaderRegistry.getClassLoader(contributionUri);
            assert loader != null;
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                    if (element.getSymbol().equals(symbol)) {
                        if (element.getValue() == null) {
                            try {
                                processorRegistry.processResource(contributionUri, resource, loader);
                            } catch (ContributionException e) {
                                throw new AssertionError("Error resolving resurce");
                            }
                        }
                        return (ResourceElement<S, ?>) element;
                    }
                }
            }
        }
        return null;
    }

    public <S extends Symbol, V> ResourceElement<S, V> resolve(URI contributionUri, Class<V> type, S symbol)
            throws MetaDataStoreException {
        Contribution contribution = find(contributionUri);
        if (contribution == null) {
            String identifier = contributionUri.toString();
            throw new ContributionResolutionException("Contribution not found", identifier);
        }
        ResourceElement<S, V> element = resolveInternal(contribution, type, symbol);
        if (element != null) {
            return element;
        }
        for (URI uri : contribution.getResolvedImportUris()) {
            Contribution resolved = cache.get(uri);
            if (resolved == null) {
                String identifier = contributionUri.toString();
                throw new ContributionResolutionException("Dependent contibution not found", identifier);
            }
            element = resolveInternal(resolved, type, symbol);
            if (element != null) {
                return element;
            }
        }
        return null;
    }

    /**
     * Resolves an import to a Contribution that exports it
     *
     * @param imprt the import to resolve
     * @return the contribution or null
     */
    public Contribution resolve(Import imprt) {
        Map<Export, Contribution> map = exportsToContributionCache.get(imprt.getType());
        if (map == null) {
            return null;
        }
        for (Map.Entry<Export, Contribution> entry : map.entrySet()) {
            int level = entry.getKey().match(imprt);
            if (level == Export.EXACT_MATCH) {
                return entry.getValue();
            }
        }
        return null;
    }

    public List<Contribution> resolveTransitiveImports(Contribution contribution) throws UnresolvableImportException {
        ArrayList<Contribution> contributions = new ArrayList<Contribution>();
        resolveTransitiveImports(contribution, contributions);
        return contributions;
    }

    private void resolveTransitiveImports(Contribution contribution, List<Contribution> dependencies)
            throws UnresolvableImportException {
        for (Import imprt : contribution.getManifest().getImports()) {
            Contribution imported = resolve(imprt);
            if (imported == null) {
                String identifier = contribution.getUri().toString();
                throw new UnresolvableImportException("Unable to resolve import in contribution", identifier, imprt);
            }
            if (!dependencies.contains(imported)) {
                dependencies.add(imported);
            }
            resolveTransitiveImports(imported, dependencies);
        }
    }

    @SuppressWarnings({"unchecked"})
    private <S extends Symbol, V> ResourceElement<S, V> resolveInternal(Contribution contribution,
                                                                        Class<V> type,
                                                                        S symbol) throws MetaDataStoreException {
        URI contributionUri = contribution.getUri();
        ClassLoader loader = classLoaderRegistry.getClassLoader(contributionUri);
        assert loader != null;
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (element.getSymbol().equals(symbol)) {
                    if (element.getValue() == null) {
                        try {
                            processorRegistry.processResource(contributionUri, resource, loader);
                        } catch (ContributionException e) {
                            String identifier = resource.getUrl().toString();
                            throw new MetaDataStoreException("Error resolving resurce", identifier, e);
                        }
                    }
                    if (!type.isInstance(element.getValue())) {
                        throw new IllegalArgumentException("Invalid type for symbol [" + type + "]");
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

        if (contribution.getManifest() == null) {
            return;
        }

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
