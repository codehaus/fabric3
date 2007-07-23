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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
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
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionProcessor;
import org.fabric3.spi.services.contribution.Import;
import org.fabric3.spi.services.contribution.MatchingExportNotFoundException;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Processes a JAR contribution
 */
public class JarContributionProcessor extends ContributionProcessorExtension implements ContributionProcessor {
    public static final QName COMPOSITE = new QName(SCA_NS, "composite");
    private static final URI HOST_CLASSLOADER = URI.create("sca://./hostClassLoader");
    private final LoaderRegistry loaderRegistry;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final XMLInputFactory xmlFactory;
    private final MetaDataStore metaDataStore;
    private final ClasspathProcessorRegistry classpathProcessorRegistry;

    public JarContributionProcessor(@Reference LoaderRegistry loaderRegistry,
                                    @Reference ClassLoaderRegistry classLoaderRegistry,
                                    @Reference XMLInputFactory xmlFactory,
                                    @Reference MetaDataStore metaDataStore,
                                    @Reference ClasspathProcessorRegistry classpathProcessorRegistry) {
        this.loaderRegistry = loaderRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.xmlFactory = xmlFactory;
        this.metaDataStore = metaDataStore;
        this.classpathProcessorRegistry = classpathProcessorRegistry;
    }

    public String getContentType() {
        return Constants.JAR_CONTENT_TYPE;
    }

    public void processContent(Contribution contribution, URI source, InputStream inputStream)
            throws ContributionException, IOException {
        URL sourceUrl = contribution.getLocation();
        // process the contribution manifest
        File jarFile = new File(sourceUrl.getFile());
        ContributionManifest manifest = processManifest(jarFile);
        contribution.setManifest(manifest);
        // process .composite files
        List<URL> artifactUrls = getCompositeUrls(inputStream, toJarURL(sourceUrl));
        // Build a classloader to perform the contribution introspection. The classpath will contain the contribution
        // jar and resolved imports
        ClassLoader cl = classLoaderRegistry.getClassLoader(HOST_CLASSLOADER);
        CompositeClassLoader loader = new CompositeClassLoader(contribution.getUri(), cl);
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
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
            for (URL artifactUrl : artifactUrls) {
                CompositeComponentType componentType = processComponentType(artifactUrl, loader);
                contribution.addType(componentType);
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

    /**
     * Returns a list of composite files inside a jar
     *
     * @param sourceUrl the root url
     * @param stream    the archive stream
     * @return a list of resources inside a jar
     * @throws IOException if an error is encountered reading the archive
     */
    private List<URL> getCompositeUrls(InputStream stream, URL sourceUrl) throws IOException {
        List<URL> artifacts = new ArrayList<URL>();
        JarInputStream jar = new JarInputStream(stream);
        try {
            while (true) {
                JarEntry entry = jar.getNextJarEntry();
                if (entry == null) {
                    // EOF
                    break;
                }
                if (entry.isDirectory()) {
                    continue;
                }
                if (entry.getName().endsWith(".composite")) {
                    artifacts.add(new URL(sourceUrl, entry.getName()));
                }
            }
        } finally {
            jar.close();
        }
        return artifacts;
    }

    private URL toJarURL(URL sourceUrl) throws MalformedURLException {
        if (sourceUrl.toString().startsWith("jar:")) {
            return sourceUrl;
        } else {
            return new URL("jar:" + sourceUrl.toExternalForm() + "!/");
        }

    }

}
