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

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.spi.services.archive.ArchiveStore;
import org.fabric3.spi.services.archive.ArchiveStoreException;
import org.fabric3.spi.services.contenttype.ContentTypeResolutionException;
import org.fabric3.spi.services.contenttype.ContentTypeResolver;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.MetaDataStoreException;
import org.fabric3.spi.services.contribution.ProcessorRegistry;

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
    private ContentTypeResolver contentTypeResolver;
    private DependencyService dependencyService;
    private String uriPrefix = "file://contribution/";
    private ContributionServiceMonitor monitor;

    public ContributionServiceImpl(@Reference ProcessorRegistry processorRegistry,
                                   @Reference ArchiveStore archiveStore,
                                   @Reference MetaDataStore metaDataStore,
                                   @Reference ContentTypeResolver contentTypeResolver,
                                   @Reference DependencyService dependencyService,
                                   @Reference MonitorFactory monitorFactory)
            throws IOException, ClassNotFoundException {
        this.processorRegistry = processorRegistry;
        this.archiveStore = archiveStore;
        this.metaDataStore = metaDataStore;
        this.contentTypeResolver = contentTypeResolver;
        this.dependencyService = dependencyService;
        this.monitor = monitorFactory.getMonitor(ContributionServiceMonitor.class);
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
            // continue processing the contributions. As they are ordered, dependencies will resolve correctly
            processContents(contribution);
        }
        List<URI> uris = new ArrayList<URI>(contributions.size());
        for (Contribution contribution : contributions) {
            uris.add(contribution.getUri());
        }
        return uris;
    }

    @Deprecated
    public URI contribute(String storeId, ContributionSource source) throws ContributionException {
        return contribute(source);
    }

    public URI contribute(ContributionSource source) throws ContributionException {
        Contribution contribution = store(source);
        processorRegistry.processManifest(contribution);
        processContents(contribution);
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
            throw new ContributionNotFoundException("No contribution found for URI", contributionUri.toString());
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
            throw new ContributionNotFoundException("Contribution not found for ", uri.toString());
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
        URI contributionUri = URI.create(uriPrefix + "/" + UUID.randomUUID());
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
            String type = contentTypeResolver.getContentType(source.getLocation());
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
     * @throws ContributionException if an error occurs during processing
     */
    private void processContents(Contribution contribution) throws ContributionException {
        // store the contribution
        processorRegistry.processContribution(contribution, contribution.getUri());
        // TODO rollback storage if an error processing contribution
        // store the contribution index
        //store the contribution in the memory cache
        try {
            metaDataStore.store(contribution);
        } catch (MetaDataStoreException e) {
            throw new ContributionException(e);
        }
    }

}
