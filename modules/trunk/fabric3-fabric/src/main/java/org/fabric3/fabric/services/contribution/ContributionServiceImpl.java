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
import org.fabric3.spi.services.archive.ArchiveStore;
import org.fabric3.spi.services.archive.ArchiveStoreException;
import org.fabric3.spi.services.contenttype.ContentTypeResolutionException;
import org.fabric3.spi.services.contenttype.ContentTypeResolver;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionStoreRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.MetaDataStoreException;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.StoreNotFoundException;

/**
 * Default ContributionService implementation
 *
 * @version $Rev$ $Date$
 */
@Service(ContributionService.class)
@EagerInit
public class ContributionServiceImpl implements ContributionService {
    private ProcessorRegistry processorRegistry;
    private ContributionStoreRegistry contributionStoreRegistry;
    private ContentTypeResolver contentTypeResolver;
    private String uriPrefix = "file://contribution/";

    public ContributionServiceImpl(@Reference ProcessorRegistry processorRegistry,
                                   @Reference ContributionStoreRegistry contributionStoreRegistry,
                                   @Reference ContentTypeResolver contentTypeResolver)
            throws IOException, ClassNotFoundException {
        this.processorRegistry = processorRegistry;
        this.contributionStoreRegistry = contributionStoreRegistry;
        this.contentTypeResolver = contentTypeResolver;
    }

    @Property(required = false)
    public void setUriPrefix(String uriPrefix) {
        this.uriPrefix = uriPrefix;
    }

    public URI contribute(String id, ContributionSource source) throws ContributionException {
        URL locationUrl;
        URI contributionUri = URI.create(uriPrefix + id + "/" + UUID.randomUUID());
        if (source.isLocal()) {
            locationUrl = source.getLocation();
        } else {
            ArchiveStore archiveStore = contributionStoreRegistry.getArchiveStore(id);
            if (archiveStore == null) {
                throw new StoreNotFoundException("Archive store not found", id);
            }
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
                    //noinspection ThrowFromFinallyBlock
                    throw new ContributionException(e);
                }
            }

        }
        try {
            String type = contentTypeResolver.getContentType(source.getLocation());
            byte[] checksum = source.getChecksum();
            long timestamp = source.getTimestamp();

            Contribution contribution = new Contribution(contributionUri, locationUrl, checksum, timestamp);
            //process the contribution
            processMetaData(id, contribution, type);
            return contributionUri;
        } catch (IOException e) {
            throw new ContributionException("Contribution error", e);
        } catch (MetaDataStoreException e) {
            throw new ContributionException("Contribution error", e);
        } catch (ContentTypeResolutionException e) {
            throw new ContributionException("Contribution error", e);
        }
    }

    public boolean exists(URI uri) {
        String id = parseStoreId(uri);
        MetaDataStore metaDataStore = contributionStoreRegistry.getMetadataStore(id);
        return metaDataStore != null && metaDataStore.find(uri) != null;
    }

    public void update(ContributionSource source) throws ContributionException {
        URI uri = source.getUri();
        byte[] checksum = source.getChecksum();
        long timestamp = source.getTimestamp();
        InputStream is = null;
        try {
            is = source.getSource();
            update(uri, checksum, timestamp, is);
        } catch (IOException e) {
            throw new ContributionException("Contribution error", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                //noinspection ThrowFromFinallyBlock
                throw new ContributionException("Contribution error", e);
            }
        }
    }

    public long getContributionTimestamp(URI uri) {
        String id = parseStoreId(uri);
        MetaDataStore metaDataStore = contributionStoreRegistry.getMetadataStore(id);
        if (metaDataStore == null) {
            return -1;
        }
        Contribution contribution = metaDataStore.find(uri);
        if (contribution == null) {
            return -1;
        }
        return contribution.getTimestamp();
    }

    public List<Deployable> getDeployables(URI contributionUri) throws ContributionException {
        String id = parseStoreId(contributionUri);
        MetaDataStore metaDataStore = contributionStoreRegistry.getMetadataStore(id);
        if (metaDataStore == null) {
            throw new StoreNotFoundException("MetaData store not found", id);
        }
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

    private void update(URI uri, byte[] checksum, long timestamp, InputStream stream)
            throws ContributionException, IOException {
        String id = parseStoreId(uri);
        MetaDataStore metaDataStore = contributionStoreRegistry.getMetadataStore(id);
        if (metaDataStore == null) {
            throw new StoreNotFoundException("MetaData store not found", id);
        }
        Contribution contribution = metaDataStore.find(uri);
        if (contribution == null) {
            throw new ContributionNotFoundException("Contribution not found for ", uri.toString());
        }
        long archivedTimestamp = contribution.getTimestamp();
        if (timestamp > archivedTimestamp) {
            // TODO update
        } else if (timestamp == archivedTimestamp && checksum.equals(contribution.getChecksum())) {
            // TODO update
        }
    }

    private void processMetaData(String id, Contribution contribution, String contentType)
            throws ContributionException, IOException, MetaDataStoreException {
        // store the contribution
        processorRegistry.processContribution(contribution, contentType, contribution.getUri());
        // TODO rollback storage if an error processing contribution
        // store the contribution index 
        MetaDataStore metaDataStore = contributionStoreRegistry.getMetadataStore(id);
        if (metaDataStore == null) {
            throw new StoreNotFoundException("MetaData store not found", id);
        }

        //store the contribution in the memory cache
        metaDataStore.store(contribution);
    }

    /**
     * Parses the store id from a contribution URI of the form <code>sca://contribution/<store id>/</code>
     *
     * @param uri the URI to parse
     * @return the store id
     */
    private String parseStoreId(URI uri) {
        String s = uri.toString();
        assert s.length() > uriPrefix.length();
        s = s.substring(uriPrefix.length());
        int index = s.indexOf("/");
        if (index > 0) {
            return s.substring(0, index);
        } else {
            return s;
        }

    }
}
