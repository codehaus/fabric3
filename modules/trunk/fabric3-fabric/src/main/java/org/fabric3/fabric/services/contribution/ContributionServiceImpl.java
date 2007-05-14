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

    public URI contribute(URL url) throws ContributionException, IOException {
        URI source;
        try {
            source = url.toURI();
        } catch (URISyntaxException e) {
            throw new InvalidContributionUriException(url.toString(), e);
        }
        URLConnection urlConnection = url.openConnection();
        String contentType = urlConnection.getContentType();
        if (contentType == null || Constants.CONTENT_UNKONWN.equals(contentType)) {
            // FIXME this should be extensible
            if (url.toExternalForm().endsWith(".jar")) {
                contentType = Constants.JAR_CONTENT_TYPE;
            }else {
                throw new AssertionError();
            }

        }
        InputStream is = urlConnection.getInputStream();
        try {
            return contribute(source, contentType, is);
        } finally {
            is.close();
        }
    }

    public URI contribute(URI sourceUri, String contentType, InputStream contributionStream)
            throws ContributionException, IOException {
        // store the contribution 
        URI contributionUri = URI.create(Constants.URI_PREFIX + UUID.randomUUID());
        URL locationURL = archiveStore.store(sourceUri, contributionStream);
        Contribution contribution = new Contribution(contributionUri, locationURL);
        //process the contribution
        InputStream stream = locationURL.openStream();
        processorRegistry.processContent(contribution, contentType, contributionUri, stream);
        // TODO rollback storage if an error processing contribution
        // index the contribution
        metaDataStore.store(contribution);
        //store the contribution in the memory cache
        return contributionUri;
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


}
