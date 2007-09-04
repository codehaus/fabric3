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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.util.FileHelper;
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
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.Resource;

/**
 * Handles exploded archives on a filesystem.
 */
public class ExplodedArchiveContributionProcessor extends ArchiveContributionProcessor {
    private LoaderRegistry loaderRegistry;
    private XMLInputFactory xmlFactory;
    private final ContentTypeResolver contentTypeResolver;

    public ExplodedArchiveContributionProcessor(@Reference LoaderRegistry loaderRegistry,
                                                @Reference ClassLoaderRegistry classLoaderRegistry,
                                                @Reference XMLInputFactory xmlFactory,
                                                @Reference(name = "metaDataStore")MetaDataStore metaDataStore,
                                                @Reference ContentTypeResolver contentTypeResolver,
                                                @Reference ArtifactLocationEncoder encoder) {
        super(metaDataStore, classLoaderRegistry, encoder);
        this.loaderRegistry = loaderRegistry;
        this.contentTypeResolver = contentTypeResolver;
        this.xmlFactory = xmlFactory;
    }

    public String[] getContentTypes() {
        return new String[] {Constants.FOLDER_CONTENT_TYPE};
    }

    protected List<URL> createClasspath(Contribution contribution) throws ContributionException {
        List<URL> libraries = new ArrayList<URL>();
        String locationDir = FileHelper.toFileString(contribution.getLocation());
        File libDir = new File(locationDir + "META-INF" + File.separatorChar + "lib");
        if (!libDir.exists()) {
            return libraries;
        }
        File[] files = libDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        try {
            for (File file : files) {
                libraries.add(file.toURL());
            }
        } catch (MalformedURLException e) {
            throw new ContributionException(e);
        }
        return libraries;
    }

    protected void processManifest(Contribution contribution) throws ContributionException {
        URL sourceUrl = contribution.getLocation();
        File file = new File(sourceUrl.getFile() + "META-INF" + File.separatorChar + "sca-contribution.xml");
        if (!file.exists()) {
            ContributionManifest manifest = new ContributionManifest();
            contribution.setManifest(manifest);
            return;
        }
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            stream = new FileInputStream(file);
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            LoaderContext context = new LoaderContextImpl(getClass().getClassLoader(), null);
            ContributionManifest manifest = loaderRegistry.load(reader, ContributionManifest.class, context);
            contribution.setManifest(manifest);
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        } catch (LoaderException e) {
            throw new ContributionException(e);
        } catch (FileNotFoundException e) {
            throw new ContributionException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    // TODO log exception
                    e.printStackTrace();
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // TODO log exception
                    e.printStackTrace();
                }
            }
        }
    }

    protected void processResources(Contribution contribution) throws ContributionException {
        File root = FileHelper.toFile(contribution.getLocation());
        assert root.isDirectory();
        processDirectory(contribution, root);
    }

    /**
     * Processes resources in a directory hierarchy, adding them to the contribution
     *
     * @param contribution the contribution
     * @param dir          the root of the directory hierarchy
     * @throws ContributionException if an error occurs processing the resource
     */
    private void processDirectory(Contribution contribution, File dir) throws ContributionException {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(contribution, dir);
            } else {
                InputStream stream = null;
                try {
                    URL entryUrl = file.toURI().toURL();
                    String contentType = contentTypeResolver.getContentType(entryUrl);
                    stream = entryUrl.openStream();
                    Resource resource = registry.processResource(contentType, stream);
                    if (resource != null) {
                        contribution.addResource(resource);
                    }
                } catch (MalformedURLException e) {
                    throw new ContributionException(e);
                } catch (IOException e) {
                    throw new ContributionException(e);
                } catch (ContentTypeResolutionException e) {
                    throw new ContributionException(e);
                } finally {
                    try {
                        if (stream != null) {
                            stream.close();
                        }
                    } catch (IOException e) {
                        // TODO log exception
                        e.printStackTrace();
                    }
                }
            }
        }

    }

}