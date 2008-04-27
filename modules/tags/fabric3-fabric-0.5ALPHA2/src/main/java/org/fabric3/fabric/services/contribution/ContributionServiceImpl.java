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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.services.archive.ArchiveStore;
import org.fabric3.spi.services.archive.ArchiveStoreException;
import org.fabric3.spi.services.contenttype.ContentTypeResolutionException;
import org.fabric3.spi.services.contenttype.ContentTypeResolver;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.MetaDataStoreException;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;

/**
 * Default ContributionService implementation
 *
 * @version $Rev$ $Date$
 */
@Service(ContributionService.class)
@EagerInit
public class ContributionServiceImpl implements ContributionService {
    private ProcessorRegistry processorRegistry;
    private ArchiveStore archiveStore;
    private MetaDataStore metaDataStore;
    private ContributionLoader contributionLoader;
    private ContentTypeResolver contentTypeResolver;
    private DependencyService dependencyService;
    private String uriPrefix = "file://contribution/";
    private ContributionServiceMonitor monitor;

    public ContributionServiceImpl(@Reference ProcessorRegistry processorRegistry,
                                   @Reference ArchiveStore archiveStore,
                                   @Reference MetaDataStore metaDataStore,
                                   @Reference ContributionLoader contributionLoader,
                                   @Reference ContentTypeResolver contentTypeResolver,
                                   @Reference DependencyService dependencyService,
                                   @Monitor ContributionServiceMonitor monitor)
            throws IOException, ClassNotFoundException {
        this.processorRegistry = processorRegistry;
        this.archiveStore = archiveStore;
        this.metaDataStore = metaDataStore;
        this.contributionLoader = contributionLoader;
        this.contentTypeResolver = contentTypeResolver;
        this.dependencyService = dependencyService;
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setUriPrefix(String uriPrefix) {
        this.uriPrefix = uriPrefix;
    }

    public List<URI> contribute(List<ContributionSource> sources) throws ContributionException {
        List<Contribution> contributions = new ArrayList<Contribution>(sources.size());
        for (ContributionSource source : sources) {
            // store the contributions
            contributions.add(store(source));
        }
        for (Contribution contribution : contributions) {
            // process any SCA manifest information, including imports and exports
            processorRegistry.processManifest(contribution);
        }
        // order the contributions based on their dependencies
        contributions = dependencyService.order(contributions);
        for (Contribution contribution : contributions) {
            ClassLoader loader = contributionLoader.loadContribution(contribution);
            // continue processing the contributions. As they are ordered, dependencies will resolve correctly
            processContents(contribution, loader);
        }
        List<URI> uris = new ArrayList<URI>(contributions.size());
        for (Contribution contribution : contributions) {
            uris.add(contribution.getUri());
        }
        return uris;
    }

    public URI contribute(ContributionSource source) throws ContributionException {
        Contribution contribution = store(source);
        processorRegistry.processManifest(contribution);
        ClassLoader loader = contributionLoader.loadContribution(contribution);
        processContents(contribution, loader);
        return contribution.getUri();
    }

    public boolean exists(URI uri) {
        return metaDataStore.find(uri) != null;
    }

    public void update(ContributionSource source) throws ContributionException {
        URI uri = source.getUri();
        byte[] checksum = source.getChecksum();
        long timestamp = source.getTimestamp();
        InputStream is = null;
        try {
            is = source.getSource();
            update(uri, checksum, timestamp);
        } catch (IOException e) {
            throw new ContributionException("Contribution error", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                monitor.error("Error closing stream", e);
            }
        }
    }

    public long getContributionTimestamp(URI uri) {
        Contribution contribution = metaDataStore.find(uri);
        if (contribution == null) {
            return -1;
        }
        return contribution.getTimestamp();
    }

    public List<Deployable> getDeployables(URI contributionUri) throws ContributionException {
        Contribution contribution = metaDataStore.find(contributionUri);
        if (contribution == null) {
            String uri = contributionUri.toString();
            throw new ContributionNotFoundException("No contribution found for: " + uri, uri);
        }
        List<Deployable> list = new ArrayList<Deployable>();
        if (contribution.getManifest() != null) {
            for (Deployable deployable : contribution.getManifest().getDeployables()) {
                list.add(deployable);
            }
        }
        return list;
    }

    public void remove(URI contributionUri) throws ContributionException {
        throw new UnsupportedOperationException();
    }

    public <T> T resolve(URI contributionUri, Class<T> definitionType, QName name) {
        throw new UnsupportedOperationException();
    }

    public URL resolve(URI contribution, String namespace, URI uri, URI baseURI) {
        throw new UnsupportedOperationException();
    }

    private void update(URI uri, byte[] checksum, long timestamp) throws ContributionException, IOException {
        Contribution contribution = metaDataStore.find(uri);
        if (contribution == null) {
            String identifier = uri.toString();
            throw new ContributionNotFoundException("Contribution not found for: " + identifier, identifier);
        }
        long archivedTimestamp = contribution.getTimestamp();
        if (timestamp > archivedTimestamp) {
            // TODO update
        } else if (timestamp == archivedTimestamp && Arrays.equals(checksum, contribution.getChecksum())) {
            // TODO update
        }
    }

    /**
     * Stores the contents of a contribution in the archive store if it is not local
     *
     * @param source the contribution source
     * @return the contribution
     * @throws ContributionException if an error occurs during the store operation
     */
    private Contribution store(ContributionSource source) throws ContributionException {
        URI contributionUri = source.getUri();
        if (contributionUri == null) {
            contributionUri = URI.create(uriPrefix + "/" + UUID.randomUUID());
        }
        URL locationUrl;
        if (!source.persist()) {
            locationUrl = source.getLocation();
        } else {
            InputStream stream = null;
            try {
                stream = source.getSource();
                locationUrl = archiveStore.store(contributionUri, stream);
            } catch (IOException e) {
                throw new ContributionException(e);
            } catch (ArchiveStoreException e) {
                throw new ContributionException(e);
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    monitor.error("Error closing contribution stream", e);
                }
            }

        }
        try {
            String type = source.getContentType();
            if (type == null) {
                type = contentTypeResolver.getContentType(source.getLocation());
            }
            byte[] checksum = source.getChecksum();
            long timestamp = source.getTimestamp();
            return new Contribution(contributionUri, locationUrl, checksum, timestamp, type);
        } catch (ContentTypeResolutionException e) {
            throw new ContributionException(e);
        }
    }

    /**
     * Processes contribution contents. This assumes all dependencies are installed and can be resolved
     *
     * @param contribution the contribution to process
     * @param loader       the classloader to load resources in
     * @throws ContributionException if an error occurs during processing
     */
    private void processContents(Contribution contribution, ClassLoader loader) throws ContributionException {
        try {
            processorRegistry.indexContribution(contribution);
            metaDataStore.store(contribution);
            processorRegistry.processContribution(contribution, loader);
            addContributionUri(contribution);
        } catch (MetaDataStoreException e) {
            throw new ContributionException(e);
        }
    }

    /**
     * Recursively adds the contribution URI to all components.
     *
     * @param contribution the contribution the component is defined in
     * @throws ContributionNotFoundException if a required imported contribution is not found
     */
    private void addContributionUri(Contribution contribution) throws ContributionException {
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                Object value = element.getValue();
                if (value instanceof Composite) {
                    addContributionUri(contribution, (Composite) value);
                }
            }
        }
    }

    /**
     * Adds the contibution URI to a component and its children if it is a composite.
     *
     * @param contribution the contribution
     * @param composite    the composite
     */
    private void addContributionUri(Contribution contribution, Composite composite) {
        for (ComponentDefinition<?> definition : composite.getComponents().values()) {
            Implementation<?> implementation = definition.getImplementation();
            if (CompositeImplementation.class.isInstance(implementation)) {
                CompositeImplementation compositeImplementation = CompositeImplementation.class.cast(implementation);
                Composite componentType = compositeImplementation.getComponentType();
                addContributionUri(contribution, componentType);
            } else {
                definition.setContributionUri(contribution.getUri());
            }
        }
    }


}
