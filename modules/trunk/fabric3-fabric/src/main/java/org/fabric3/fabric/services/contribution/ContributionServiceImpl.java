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
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.model.type.ContributionResourceDescription;
import org.fabric3.spi.model.type.Implementation;
import org.fabric3.spi.services.contribution.ArchiveStore;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionProcessorRegistry;
import org.fabric3.spi.services.contribution.Deployable;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Default ContributionService implementation
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ContributionServiceImpl implements ContributionService {
    private final ArchiveStore archiveStore;
    private final MetaDataStore metaDataStore;
    private final ContributionProcessorRegistry processorRegistry;

    public ContributionServiceImpl(@Reference MetaDataStore metaDataStore,
                                   @Reference ArchiveStore archiveStore,
                                   @Reference ContributionProcessorRegistry processorRegistry)
            throws IOException, ClassNotFoundException {
        this.metaDataStore = metaDataStore;
        this.archiveStore = archiveStore;
        this.processorRegistry = processorRegistry;
    }

    public URI contribute(URL url, byte[] checksum, long timestamp) throws ContributionException, IOException {
        URI source;
        try {
            source = url.toURI();
        } catch (URISyntaxException e) {
            throw new InvalidContributionUriException(url.toString(), e);
        }
        URLConnection urlConnection = url.openConnection();
        String contentType = getContentType(urlConnection, url);
        InputStream is = urlConnection.getInputStream();
        try {
            return contribute(source, contentType, checksum, timestamp, is);
        } finally {
            is.close();
        }
    }

    private String getContentType(URLConnection urlConnection, URL url) {
        String contentType = urlConnection.getContentType();
        if (contentType == null || Constants.CONTENT_UNKONWN.equals(contentType)) {
            // FIXME this should be extensible
            if (url.toExternalForm().endsWith(".jar")) {
                contentType = Constants.JAR_CONTENT_TYPE;
            } else {
                throw new AssertionError();
            }

        }
        return contentType;
    }

    public URI contribute(URI sourceUri, String contentType, byte[] checksum, long timestamp, InputStream sourceStream)
            throws ContributionException, IOException {
        // store the contribution
        URI contributionUri = URI.create(Constants.URI_PREFIX + UUID.randomUUID());
        URL locationURL = archiveStore.store(sourceUri, sourceStream);
        Contribution contribution = new Contribution(contributionUri, locationURL, checksum, timestamp);
        //process the contribution
        InputStream stream = locationURL.openStream();
        processorRegistry.processContent(contribution, contentType, contributionUri, stream);
        // TODO rollback storage if an error processing contribution
        // index the contribution
        addContributionDescription(contribution);
        metaDataStore.store(contribution);
        //store the contribution in the memory cache
        return contributionUri;
    }

    public boolean exists(URI uri) {
        return metaDataStore.find(uri) != null;
    }

    public void update(URI uri, byte[] checksum, long timestamp, URL url) throws ContributionException, IOException {
        URLConnection urlConnection = url.openConnection();
        String contentType = getContentType(urlConnection, url);
        InputStream is = urlConnection.getInputStream();
        try {
            update(uri, contentType, checksum, timestamp, is);
        } finally {
            is.close();
        }

    }

    public void update(URI uri, String contentType, byte[] checksum, long timestamp, InputStream stream)
            throws ContributionException, IOException {
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

    public long getContributionTimestamp(URI uri) {
        Contribution contribution = metaDataStore.find(uri);
        if (contribution == null) {
            return -1;
        }
        return contribution.getTimestamp();
    }

    public List<QName> getDeployables(URI contributionUri) throws ContributionNotFoundException {
        Contribution contribution = metaDataStore.find(contributionUri);
        if (contribution == null) {
            throw new ContributionNotFoundException("No contribution found for URI", contributionUri.toString());
        }
        List<QName> list = new ArrayList<QName>();
        for (Deployable deployable : contribution.getManifest().getDeployables()) {
            list.add(deployable.getName());
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

    /**
     * Recursively adds a resource description pointing to the contribution artifact on contained components.
     *
     * @param contribution the contribution the resource description requires
     */
    private void addContributionDescription(Contribution contribution) {
        ContributionResourceDescription description = new ContributionResourceDescription(contribution.getLocation());
        for (CompositeComponentType type : contribution.getComponentTypes().values()) {
            addContributionDescription(description, type);
        }
    }

    /**
     * Adds the given resource description pointing to the contribution artifact on contained components.
     *
     * @param description the resource description
     * @param type        the component type to introspect
     */
    private void addContributionDescription(ContributionResourceDescription description, CompositeComponentType type) {
        for (ComponentDefinition<?> definition : type.getComponents().values()) {
            Implementation<?> implementation = definition.getImplementation();
            if (CompositeImplementation.class.isInstance(implementation)) {
                CompositeImplementation compositeImplementation = CompositeImplementation.class.cast(implementation);
                CompositeComponentType componentType = compositeImplementation.getComponentType();
                addContributionDescription(description, componentType);
            } else {
                implementation.addResourceDescription(description);
            }
        }
    }

}
