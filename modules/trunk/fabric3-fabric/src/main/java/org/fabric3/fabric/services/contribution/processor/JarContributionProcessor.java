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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.contribution.ArchiveContributionProcessor;
import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.spi.deployer.CompositeClassLoader;
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
import org.fabric3.spi.services.contribution.Import;
import org.fabric3.spi.services.contribution.MatchingExportNotFoundException;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.Resource;

/**
 * Processes a JAR contribution
 */
public class JarContributionProcessor extends ArchiveContributionProcessor implements ContributionProcessor {
    public static final QName COMPOSITE = new QName(SCA_NS, "composite");
    private static final URI HOST_CLASSLOADER = URI.create("sca://./hostClassLoader");
    private final LoaderRegistry loaderRegistry;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final XMLInputFactory xmlFactory;
    private final MetaDataStore metaDataStore;
    private final ClasspathProcessorRegistry classpathProcessorRegistry;
    private final ContentTypeResolver contentTypeResolver;

    public JarContributionProcessor(@Reference ProcessorRegistry processorRegistry,
                                    @Reference LoaderRegistry loaderRegistry,
                                    @Reference ClassLoaderRegistry classLoaderRegistry,
                                    @Reference XMLInputFactory xmlFactory,
                                    @Reference MetaDataStore metaDataStore,
                                    @Reference ClasspathProcessorRegistry classpathProcessorRegistry,
                                    @Reference ArtifactLocationEncoder encoder,
                                    @Reference ContentTypeResolver contentTypeResolver) {

        super(metaDataStore, encoder);
        this.registry = processorRegistry;
        this.loaderRegistry = loaderRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.xmlFactory = xmlFactory;
        this.metaDataStore = metaDataStore;
        this.classpathProcessorRegistry = classpathProcessorRegistry;
        this.contentTypeResolver = contentTypeResolver;
    }

    public String getContentType() {
        return Constants.JAR_CONTENT_TYPE;
    }

    public void processContent(Contribution contribution, URI source) throws ContributionException {
        URL sourceUrl = contribution.getLocation();
        InputStream inputStream;
        // process the contribution manifest
        File jarFile = new File(sourceUrl.getFile());
        ContributionManifest manifest;
        try {
            inputStream = sourceUrl.openStream();
            manifest = processManifest(jarFile);
            contribution.setManifest(manifest);
        } catch (IOException e) {
            throw new ContributionException(e);
        }
        // Build a classloader to perform the contribution introspection. The classpath will contain the contribution
        // jar and resolved imports
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = classLoaderRegistry.getClassLoader(HOST_CLASSLOADER);
        CompositeClassLoader loader = new CompositeClassLoader(contribution.getUri(), cl);
        loader.addParent(oldClassloader);
        try {
            List<URL> classpath = classpathProcessorRegistry.process(jarFile);
            for (URL library : classpath) {
                loader.addURL(library);
            }
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
            // set the classloader on the current context so artifacts in the contribution can be introspected
            Thread.currentThread().setContextClassLoader(loader);
            processResources(contribution);
            addContributionDescription(contribution);
        } catch (IOException e) {
            throw new ContributionException(e);
        } finally {
            try {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new ContributionException(e);
                }
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassloader);
            }
        }
    }

    private void processResources(Contribution contribution) throws ContributionException, IOException {
        URL location = contribution.getLocation();
        JarInputStream jarStream = new JarInputStream(location.openStream());
        try {
            while (true) {
                JarEntry entry = jarStream.getNextJarEntry();
                if (entry == null) {
                    // EOF
                    break;
                }
                if (entry.isDirectory()) {
                    continue;
                }
                URL entryUrl = new URL(location, entry.getName());
                String contentType = contentTypeResolver.getContentType(entryUrl);
                Resource resource = registry.processResource(contentType, jarStream);
                if (resource != null) {
                    contribution.addResource(resource);
                }
            }
        } catch (ContentTypeResolutionException e) {
            throw new ContributionException(e);
        } finally {
            jarStream.close();
        }
    }

    /**
     * Loads the sca manifest
     * <p/>
     * TODO support generated manifests
     *
     * @param file pointer to the jar file
     * @return the loaded manifest
     * @throws IOException           if an error occurs reading the manifest
     * @throws ContributionException if an error occurs processing the manifest
     */
    private ContributionManifest processManifest(File file) throws IOException, ContributionException {
        JarFile jar = null;
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("META-INF/sca-contribution.xml");
            if (entry == null) {
                throw new ContributionManifestNotFoundException(file.getCanonicalPath());
            }
            stream = jar.getInputStream(entry);
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            // FIXME SCDL location
            LoaderContext context = new LoaderContextImpl(getClass().getClassLoader(), null);
            return loaderRegistry.load(reader, ContributionManifest.class, context);
        } catch (FileNotFoundException e) {
            throw new ContributionManifestNotFoundException(file.toString());
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        } catch (LoaderException e) {
            throw new ContributionException(e);
        } finally {
            if (jar != null) {
                jar.close();
            }
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

}
