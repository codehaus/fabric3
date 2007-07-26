/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.maven;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.contribution.ContributionProcessorExtension;
import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.archive.ArchiveStore;
import org.fabric3.spi.services.archive.ArchiveStoreException;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionStoreRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.MetaDataStoreException;
import org.fabric3.spi.services.contribution.StoreNotFoundException;

/**
 * @version $Rev$ $Date$
 */
public class MavenContributionProcessor extends ContributionProcessorExtension {

    private ContributionStoreRegistry contributionStoreRegistry;
    private String extensionsStoreId = "maven";

    public MavenContributionProcessor(@Reference ContributionStoreRegistry contributionStoreRegistry) {
        this.contributionStoreRegistry = contributionStoreRegistry;
    }

    public String getContentType() {
        return null;
    }

    @Property(required = false)
    public void setExtensionsStoreId(String extensionsStoreId) {
        this.extensionsStoreId = extensionsStoreId;
    }

    public void processContent(Contribution contribution, URI source, InputStream inputStream)
            throws ContributionException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            copy(inputStream, outputStream);
            String[] extensionUris = outputStream.toString().split(";");
            if (extensionUris.length == 1 && extensionUris[0].length() == 0) {
                return;
            }
            for (String uri : extensionUris) {
                processExtension(contribution, uri);
            }
        } catch (IOException e) {
            throw new ContributionException(e);
        }
    }

    @SuppressWarnings({"ThrowFromFinallyBlock"})
    private List<Contribution> processExtension(Contribution contribution, String extensionUri)
            throws ContributionException {
        List<Contribution> contributions = new ArrayList<Contribution>();
        ArchiveStore archiveStore = contributionStoreRegistry.getArchiveStore(extensionsStoreId);
        if (archiveStore == null) {
            throw new StoreNotFoundException("Extensions archive store not found", extensionsStoreId);
        }
        MetaDataStore metaDataStore = contributionStoreRegistry.getMetadataStore(extensionsStoreId);
        if (metaDataStore == null) {
            throw new StoreNotFoundException("Extensions metadata store not found", extensionsStoreId);
        }
        InputStream stream = null;
        InputStream archiveStream = null;
        try {
            URI contributionUri = URI.create(contribution.getUri() + "/" + UUID.randomUUID());

            URL url = archiveStore.find(URI.create(extensionUri));
            if (url == null) {
                throw new MavenArtifactNotFoundException("Unable to resolve artifact", extensionUri);
            }
            archiveStream = url.openStream();
            stream = url.openStream();
            Contribution child = new Contribution(contributionUri, url, new byte[0], -1);
            contributions.add(child);
            registry.processContent(child, Constants.JAR_CONTENT_TYPE, url.toURI(), stream);
            metaDataStore.store(child);
        } catch (ArchiveStoreException e) {
            throw new ContributionException(e);
        } catch (MetaDataStoreException e) {
            throw new ContributionException(e);
        } catch (IOException e) {
            throw new ContributionException(e);
        } catch (URISyntaxException e) {
            throw new ContributionException(e);
        } finally {
            try {
                if (archiveStream != null) {
                    try {
                        archiveStream.close();
                    } catch (IOException e) {
                        throw new ContributionException(e);
                    }
                }
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        throw new ContributionException(e);
                    }
                }
            }
        }
        return contributions;

    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
