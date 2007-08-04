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

package org.fabric3.fabric.services.contribution.processor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contenttype.ContentTypeResolutionException;
import org.fabric3.spi.services.contenttype.ContentTypeResolver;
import org.fabric3.spi.services.contribution.ArtifactLocationEncoder;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionProcessor;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.Resource;

/**
 * Introspects a Zip-based contribution, delegating to ResourceProcessors for handling leaf-level children.
 */
public class ZipContributionProcessor extends ArchiveContributionProcessor implements ContributionProcessor {
    public static final QName COMPOSITE = new QName(SCA_NS, "composite");
    private final LoaderRegistry loaderRegistry;
    private final XMLInputFactory xmlFactory;
    private final ClasspathProcessorRegistry classpathProcessorRegistry;
    private final ContentTypeResolver contentTypeResolver;

    public ZipContributionProcessor(@Reference ProcessorRegistry processorRegistry,
                                    @Reference LoaderRegistry loaderRegistry,
                                    @Reference ClassLoaderRegistry classLoaderRegistry,
                                    @Reference XMLInputFactory xmlFactory,
                                    @Reference MetaDataStore metaDataStore,
                                    @Reference ClasspathProcessorRegistry classpathProcessorRegistry,
                                    @Reference ArtifactLocationEncoder encoder,
                                    @Reference ContentTypeResolver contentTypeResolver) {

        super(metaDataStore, classLoaderRegistry, encoder);
        this.registry = processorRegistry;
        this.loaderRegistry = loaderRegistry;
        this.xmlFactory = xmlFactory;
        this.classpathProcessorRegistry = classpathProcessorRegistry;
        this.contentTypeResolver = contentTypeResolver;
    }

    public String getContentType() {
        return Constants.ZIP_CONTENT_TYPE;
    }

    protected void processResources(Contribution contribution) throws ContributionException {
        URL location = contribution.getLocation();
        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(location.openStream());
            while (true) {
                ZipEntry entry = zipStream.getNextEntry();
                if (entry == null) {
                    // EOF
                    break;
                }
                if (entry.isDirectory()) {
                    continue;
                }
                URL entryUrl = new URL(location, entry.getName());
                String contentType = contentTypeResolver.getContentType(entryUrl);
                Resource resource = registry.processResource(contentType, zipStream);
                if (resource != null) {
                    contribution.addResource(resource);
                }
            }
        } catch (ContentTypeResolutionException e) {
            throw new ContributionException(e);
        } catch (MalformedURLException e) {
            throw new ContributionException(e);
        } catch (IOException e) {
            throw new ContributionException(e);
        } finally {
            try {
                if (zipStream != null) {
                    zipStream.close();
                }
            } catch (IOException e) {
                //noinspection ThrowFromFinallyBlock
                throw new ContributionException(e);
            }
        }
    }

    @SuppressWarnings({"ThrowFromFinallyBlock"})
    protected void processManifest(Contribution contribution) throws ContributionException {
        ZipFile zip = null;
        XMLStreamReader reader = null;
        try {
            URL sourceUrl = contribution.getLocation();
            zip = new ZipFile(sourceUrl.getFile());
            ZipEntry entry = zip.getEntry("META-INF/sca-contribution.xml");
            if (entry == null) {
                ContributionManifest manifest = new ContributionManifest();
                contribution.setManifest(manifest);
                return;
            }
            InputStream stream = zip.getInputStream(entry);
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            LoaderContext context = new LoaderContextImpl(getClass().getClassLoader(), null);
            ContributionManifest manifest = loaderRegistry.load(reader, ContributionManifest.class, context);
            contribution.setManifest(manifest);
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        } catch (LoaderException e) {
            throw new ContributionException(e);
        } catch (IOException e) {
            throw new ContributionException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    // ignore
                }
            }
            if (zip != null) {
                try {
                    // close zip and input stream
                    zip.close();
                } catch (IOException e) {
                    throw new ContributionException(e);
                }
            }
        }
    }

    protected List<URL> createClasspath(Contribution contribution) throws ContributionException {
        try {
            return classpathProcessorRegistry.process(new File(contribution.getLocation().getFile()));
        } catch (IOException e) {
            throw new ContributionException(e);
        }

    }

}
