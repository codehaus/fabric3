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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.contribution.ContributionProcessorExtension;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.deployer.CompositeClassLoader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionProcessor;
import org.fabric3.spi.services.contribution.Import;
import org.fabric3.spi.services.contribution.MatchingExportNotFoundException;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Handles exploded jars on a filesystem.
 */
public class ExplodedJarContributionProcessor extends ContributionProcessorExtension implements ContributionProcessor {
    private static final URI HOST_CLASSLOADER = URI.create("sca://./hostClassLoader");
    private LoaderRegistry loaderRegistry;
    private ClassLoaderRegistry classLoaderRegistry;
    private XMLInputFactory xmlFactory;
    private MetaDataStore metaDataStore;

    public ExplodedJarContributionProcessor(@Reference LoaderRegistry loaderRegistry,
                                       @Reference ClassLoaderRegistry classLoaderRegistry,
                                       @Reference XMLInputFactory xmlFactory,
                                       @Reference MetaDataStore metaDataStore) {
        this.loaderRegistry = loaderRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.xmlFactory = xmlFactory;
        this.metaDataStore = metaDataStore;
    }

    public String getContentType() {
        return Constants.FOLDER_CONTENT_TYPE;
    }

    public void processContent(Contribution contribution, URI source, InputStream inputStream)
            throws ContributionException, IOException {
        URL sourceUrl = contribution.getLocation();
        // process the contribution manifest
        ContributionManifest manifest = processManifest(sourceUrl);
        contribution.setManifest(manifest);
        // process .composite files
        List<URL> artifactUrls = getCompositeUrls(sourceUrl);
        // Build a classloader to perform the contribution introspection. The classpath will contain the contribution
        // jar and resolved imports
        // FIXME for now, add the jar to the system classloader
        ClassLoader cl = classLoaderRegistry.getClassLoader(HOST_CLASSLOADER);
        CompositeClassLoader loader = new CompositeClassLoader(contribution.getUri(), cl);
        loader.addURL(sourceUrl);
        for (Import imprt : manifest.getImports()) {
            Contribution imported = metaDataStore.resolve(imprt);
            if (imported == null) {
                throw new MatchingExportNotFoundException(imprt.toString());
            }
            // add the resolved URI to the contribution
            contribution.addResolvedImportUri(imported.getUri());
            // add the jar to the classpath
            loader.addURL(imported.getLocation());
        }
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        try {
            // set the classloader on the current context so artifacts in the contribution can be introspected
            Thread.currentThread().setContextClassLoader(loader);
            for (URL artifactUrl : artifactUrls) {
                CompositeComponentType componentType = processComponentType(artifactUrl, loader);
                contribution.addType(componentType.getName(), componentType);
            }
        } catch (LoaderException e) {
            throw new ContributionException(e);
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }
    }

    /**
     * Loads the sca manifest
     * <p/>
     * TODO support generated manifests
     *
     * @param sourceUrl the base url to load the manifest from
     * @return the loaded manifest
     * @throws IOException           if an error occurs reading the manifest
     * @throws ContributionException if an error occurs processing the manifest
     */
    private ContributionManifest processManifest(URL sourceUrl)
            throws IOException, ContributionException {
        URL manifest = new URL(sourceUrl.toExternalForm() + "META-INF/sca-contribution.xml");
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            stream = manifest.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            // FIXME SCDL location
            LoaderContext context = new LoaderContextImpl(getClass().getClassLoader(), null);
            return loaderRegistry.load(reader, ContributionManifest.class, context);
        } catch (FileNotFoundException e) {
            throw new ContributionManifestNotFoundException(sourceUrl.toString());
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        } catch (LoaderException e) {
            throw new ContributionException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    // ignore
                }
            }
            if (stream != null) {
                stream.close();
            }
        }
    }

    /**
     * Loads a composite component type at the given URL
     *
     * @param artifactUrl the URL to load from
     * @param loader      the classloader to load resources with
     * @return the component type
     * @throws IOException        if an error occurs reading the URL stream
     * @throws XMLStreamException if an error occurs parsing the XML
     * @throws LoaderException    if an error occurs processing the component type
     */
    private CompositeComponentType processComponentType(URL artifactUrl, ClassLoader loader)
            throws IOException, XMLStreamException, LoaderException {
        XMLStreamReader reader = null;
        InputStream stream = null;
        try {
            stream = artifactUrl.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            LoaderContext context = new LoaderContextImpl(loader, null);
            return loaderRegistry.load(reader, CompositeComponentType.class, context);

        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                // ignore
            }
            if (stream != null) {
                stream.close();
            }
        }
    }

    private List<URL> getCompositeUrls(URL sourceUrl) throws IOException, ContributionException {
        List<URL> artifacts = new ArrayList<URL>();
        try {
            URL manifestUrl = new URL(sourceUrl.toExternalForm() + "META-INF");
            File manifestDir = new File(manifestUrl.toURI());
            File[] files = manifestDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".composite");
                }
            });
            for (File file : files) {
                artifacts.add(file.toURL());

            }
        } catch (URISyntaxException e) {
            throw new ContributionException("Illegal URL", e);
        }
        return artifacts;
    }


}