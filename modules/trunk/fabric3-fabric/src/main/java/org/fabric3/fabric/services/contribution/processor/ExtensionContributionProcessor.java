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
package org.fabric3.fabric.services.contribution.processor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.contribution.ContributionProcessorExtension;
import static org.fabric3.fabric.runtime.ComponentNames.BOOT_CLASSLOADER_ID;
import org.fabric3.spi.services.contribution.StoreNotFoundException;
import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Include;
import org.fabric3.scdl.ModelObject;
import static org.fabric3.spi.Constants.FABRIC3_SYSTEM_NS;
import org.fabric3.spi.services.archive.ArchiveStore;
import org.fabric3.spi.services.archive.ArchiveStoreException;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionProcessor;
import org.fabric3.spi.services.contribution.ContributionProcessorRegistry;
import org.fabric3.spi.services.contribution.ContributionStoreRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.MetaDataStoreException;

/**
 * Processes extension jars in a directory. A Contribution is created with one deployable composite for all extensions,
 * the extensions composite. The processor dispatches back to the ContributionProcessorRegistry to introspect the
 * individual extension jars. All deployable composites in extension jars are included in the extensions composite. The
 * extension jars are then added to the Contribution as resolved imported contributions.
 */
public class ExtensionContributionProcessor extends ContributionProcessorExtension implements ContributionProcessor {
    private ContributionProcessorRegistry registry;
    private ClassLoaderRegistry classLoaderRegistry;
    private ContributionStoreRegistry contributionStoreRegistry;
    private String extensionsStoreId = "extensions";

    public ExtensionContributionProcessor(@Reference ContributionProcessorRegistry registry,
                                          @Reference ClassLoaderRegistry classLoaderRegistry,
                                          @Reference ContributionStoreRegistry contributionStoreRegistry) {
        this.registry = registry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.contributionStoreRegistry = contributionStoreRegistry;
    }

    @Property(required = false)
    public void setExtensionsStoreId(String extensionsStoreId) {
        this.extensionsStoreId = extensionsStoreId;
    }

    public String getContentType() {
        return Constants.EXTENSION_TYPE;
    }

    public void processContent(Contribution contribution, URI source, InputStream inputStream)
            throws ContributionException {
        URL sourceUrl = contribution.getLocation();
        File sourceDir = new File(sourceUrl.getFile());
        File[] jars = sourceDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jar");
            }
        });
        ContributionManifest manifest = new ContributionManifest();
        contribution.setManifest(manifest);
        if (jars.length == 0) {
            return;
        }
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        ClassLoader bootCl = classLoaderRegistry.getClassLoader(BOOT_CLASSLOADER_ID);
        List<Contribution> contributions = null;
        try {
            // Set the boot classloader so SPI classes are available to extensions
            // This will need to change in the future
            Thread.currentThread().setContextClassLoader(bootCl);
            contributions = processArchives(contribution, jars);
        } catch (ArchiveStoreException e) {
            throw new ContributionException(e);
        } catch (IOException e) {
            throw new ContributionException(e);
        } catch (MetaDataStoreException e) {
            throw new ContributionException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }
        QName name = new QName(FABRIC3_SYSTEM_NS, "extensions");
        createComponentType(contribution, name, contributions);
        manifest.addDeployable(new Deployable(name, Constants.COMPOSITE_TYPE));
    }

    private List<Contribution> processArchives(Contribution contribution, File[] jars)
            throws IOException, ContributionException, ArchiveStoreException, MetaDataStoreException {
        ArchiveStore archiveStore = contributionStoreRegistry.getArchiveStore(extensionsStoreId);
        if (archiveStore == null) {
            throw new StoreNotFoundException("Extensions archive store not found", extensionsStoreId);
        }
        MetaDataStore metaDataStore = contributionStoreRegistry.getMetadataStore(extensionsStoreId);
        if (metaDataStore == null) {
            throw new StoreNotFoundException("Extensions metadata store not found", extensionsStoreId);
        }
        List<Contribution> contributions = new ArrayList<Contribution>();
        for (File jar : jars) {
            InputStream stream = null;
            InputStream archiveStream = null;
            try {
                URI contributionUri = URI.create(contribution.getUri() + "/" + UUID.randomUUID());
                URL url = jar.toURI().toURL();
                archiveStream = url.openStream();
                archiveStore.store(contributionUri, archiveStream);
                stream = url.openStream();
                Contribution child = new Contribution(contributionUri, url, new byte[0], -1);
                contributions.add(child);
                registry.processContent(child, Constants.JAR_CONTENT_TYPE, jar.toURI(), stream);
                metaDataStore.store(child);
            } finally {
                try {
                    if (archiveStream != null) {
                        archiveStream.close();
                    }
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            }
        }
        return contributions;
    }

    private void createComponentType(Contribution contribution, QName qName, List<Contribution> contributions) {
        Composite type = new Composite(qName);
        for (Contribution child : contributions) {
            for (Map.Entry<QName, ModelObject> entry : child.getTypes().entrySet()) {
                if (!(entry.getValue() instanceof Composite)) {
                    continue;
                }
                QName name = entry.getKey();
                Composite childType = (Composite) entry.getValue();
                for (Deployable deployable : child.getManifest().getDeployables()) {
                    if (deployable.getName().equals(name)) {
                        Include include = new Include();
                        include.setName(name);
                        include.setIncluded(childType);
                        type.add(include);
                        break;
                    }
                }
                contribution.addType(name, childType);
            }
            for (URI uri : child.getResolvedImportUris()) {
                contribution.addResolvedImportUri(uri);
            }
            contribution.addResolvedImportUri(child.getUri());
        }
        contribution.addType(qName, type);
    }

}
