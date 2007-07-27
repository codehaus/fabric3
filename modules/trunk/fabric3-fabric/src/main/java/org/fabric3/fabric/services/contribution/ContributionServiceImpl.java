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

import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ModelObject;
import org.fabric3.spi.model.type.ContributionResourceDescription;
import org.fabric3.spi.services.archive.ArchiveStore;
import org.fabric3.spi.services.archive.ArchiveStoreException;
import org.fabric3.spi.services.contribution.ArtifactLocationEncoder;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionProcessorRegistry;
import org.fabric3.spi.services.contribution.ContributionStoreRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.MetaDataStoreException;
import org.fabric3.spi.services.contribution.StoreNotFoundException;

/**
 * Default ContributionService implementation
 *
 * @version $Rev$ $Date$
 */
@Service(ContributionService.class)
@EagerInit
public class ContributionServiceImpl implements ContributionService {
    /**
     * The contribution prefix. Default is to map directly to the file system, which assumes nodes have access to it
     */
    private String uriPrefix = "file://contribution/";
    private ContributionProcessorRegistry processorRegistry;
    private ArtifactLocationEncoder encoder;
    private ContributionStoreRegistry contributionStoreRegistry;

    public ContributionServiceImpl(@Reference ContributionProcessorRegistry processorRegistry,
                                   @Reference ArtifactLocationEncoder encoder,
                                   @Reference ContributionStoreRegistry contributionStoreRegistry)
            throws IOException, ClassNotFoundException {
        this.processorRegistry = processorRegistry;
        this.encoder = encoder;
        this.contributionStoreRegistry = contributionStoreRegistry;
    }

    @Property(required = false)
    public void setUriPrefix(String uriPrefix) {
        this.uriPrefix = uriPrefix;
    }

    public URI contribute(String id, ContributionSource source) throws ContributionException {
        URL locationUrl;
        URI contributionUri = URI.create(uriPrefix + id + "/" + UUID.randomUUID());
        InputStream sourceStream;
        if (source.isLocal()) {
            try {
                locationUrl = source.getLocation();
                sourceStream = source.getSource();
            } catch (IOException e) {
                throw new ContributionException(e);
            }
        } else {
            ArchiveStore archiveStore = contributionStoreRegistry.getArchiveStore(id);
            if (archiveStore == null) {
                throw new StoreNotFoundException("Archive store not found", id);
            }
            InputStream stream = null;
            try {
                stream = source.getSource();
                locationUrl = archiveStore.store(contributionUri, stream);
                sourceStream = locationUrl.openStream();
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
            String type = getContentType(locationUrl, source.getContentType());
            byte[] checksum = source.getChecksum();
            long timestamp = source.getTimestamp();

            Contribution contribution = new Contribution(contributionUri, locationUrl, checksum, timestamp);
            //process the contribution
            processMetaData(id, contribution, type, sourceStream);
            return contributionUri;
        } catch (IOException e) {
            throw new ContributionException("Contribution error", e);
        } catch (MetaDataStoreException e) {
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
        for (Deployable deployable : contribution.getManifest().getDeployables()) {
            list.add(deployable);
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

    private void processMetaData(String id, Contribution contribution, String contentType, InputStream stream)
            throws ContributionException, IOException, MetaDataStoreException {
        // store the contribution
        //Contribution contribution = new Contribution(contributionUri, locationUrl, checksum, timestamp);
        //process the contribution
        //InputStream stream = locationUrl.openStream();
        processorRegistry.processContent(contribution, contentType, contribution.getUri(), stream);
        // TODO rollback storage if an error processing contribution
        // index the contribution
        addContributionDescription(contribution);
        MetaDataStore metaDataStore = contributionStoreRegistry.getMetadataStore(id);
        if (metaDataStore == null) {
            throw new StoreNotFoundException("MetaData store not found", id);
        }

        //store the contribution in the memory cache
        metaDataStore.store(contribution);
    }

    private String getContentType(URL url, String contentType) {
        // FIXME Replace everything in this method with the activation-based content type registry
        if (contentType == null || Constants.CONTENT_UNKONWN.equals(contentType)) {
            if (url.toExternalForm().endsWith(".jar")) {
                return Constants.JAR_CONTENT_TYPE;
            } else {
                throw new AssertionError();
            }

        } else if (Constants.EXTENSION_TYPE.equals(contentType)) {
            return contentType;
        } else if (url != null) {
            if ("file".equals(url.getProtocol())) {
                return Constants.FOLDER_CONTENT_TYPE;
            }
        }
        return contentType;
    }

    /**
     * Recursively adds a resource description pointing to the contribution artifact on contained components.
     *
     * @param contribution the contribution the resource description requires
     * @throws StoreNotFoundException        if no store can be found for a contribution import
     * @throws ContributionNotFoundException if a required imported contribution is not found
     */
    private void addContributionDescription(Contribution contribution)
            throws StoreNotFoundException, ContributionNotFoundException {
        // encode the contribution URL so it can be dereferenced remotely
        URL identifier = encoder.encode(contribution.getLocation());
        ContributionResourceDescription description = new ContributionResourceDescription(identifier);
        // Obtain local URLs for imported contributions and encode them for remote dereferencing
        for (URI uri : contribution.getResolvedImportUris()) {
            String key = parseStoreId(uri);
            MetaDataStore store = contributionStoreRegistry.getMetadataStore(key);
            if (store == null) {
                throw new StoreNotFoundException("No store for id", key);
            }
            Contribution imported = store.find(uri);
            if (imported == null) {
                throw new ContributionNotFoundException("Imported contribution not found", uri.toString());
            }
            URL importedUrl = encoder.encode(imported.getLocation());
            description.addArtifactUrl(importedUrl);
        }
        for (ModelObject type : contribution.getTypes().values()) {
            if (type instanceof Composite) {
                addContributionDescription(description, (Composite) type);
            }
        }
    }

    /**
     * Adds the given resource description pointing to the contribution artifact on contained components.
     *
     * @param description the resource description
     * @param type        the component type to introspect
     */
    private void addContributionDescription(ContributionResourceDescription description, Composite type) {
        for (ComponentDefinition<?> definition : type.getComponents().values()) {
            Implementation<?> implementation = definition.getImplementation();
            if (CompositeImplementation.class.isInstance(implementation)) {
                CompositeImplementation compositeImplementation = CompositeImplementation.class.cast(implementation);
                Composite componentType = compositeImplementation.getComponentType();
                addContributionDescription(description, componentType);
            } else {
                implementation.addResourceDescription(description);
            }
        }
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
