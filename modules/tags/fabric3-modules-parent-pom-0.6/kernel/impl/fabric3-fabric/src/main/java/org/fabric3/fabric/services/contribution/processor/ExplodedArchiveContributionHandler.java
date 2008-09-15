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

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.spi.services.contenttype.ContentTypeResolutionException;
import org.fabric3.spi.services.contenttype.ContentTypeResolver;
import org.fabric3.spi.services.contribution.Action;
import org.fabric3.spi.services.contribution.ArchiveContributionHandler;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ProcessorRegistry;

/**
 * Handles exploded archives on a filesystem.
 */
public class ExplodedArchiveContributionHandler implements ArchiveContributionHandler {
    private Loader loader;
    private final ContentTypeResolver contentTypeResolver;
    private ProcessorRegistry registry;


    public ExplodedArchiveContributionHandler(@Reference Loader loader,
                                              @Reference ContentTypeResolver contentTypeResolver,
                                              @Reference ProcessorRegistry registry) {
        this.loader = loader;
        this.contentTypeResolver = contentTypeResolver;
        this.registry = registry;
    }

    public String getContentType() {
        return Constants.FOLDER_CONTENT_TYPE;
    }

    public boolean canProcess(Contribution contribution) {
        return Constants.FOLDER_CONTENT_TYPE.equals(contribution.getContentType());
    }

    public void processManifest(Contribution contribution, final ValidationContext context) throws ContributionException {
        ContributionManifest manifest;
        try {
            URL sourceUrl = contribution.getLocation();
            URL manifestUrl = new URL(sourceUrl.toString() + "/META-INF/sca-contribution.xml");
            ClassLoader cl = getClass().getClassLoader();
            URI uri = contribution.getUri();
            IntrospectionContext childContext = new DefaultIntrospectionContext(cl, uri, null);
            manifest = loader.load(manifestUrl, ContributionManifest.class, childContext);
            if (childContext.hasErrors()) {
                context.addErrors(childContext.getErrors());
            }
            if (childContext.hasWarnings()) {
                context.addWarnings(childContext.getWarnings());
            }
        } catch (LoaderException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                manifest = new ContributionManifest();
            } else {
                throw new ContributionException(e);
            }
        } catch (MalformedURLException e) {
            manifest = new ContributionManifest();
        }
        contribution.setManifest(manifest);

        iterateArtifacts(contribution, new Action() {
            public void process(Contribution contribution, String contentType, URL url)
                    throws ContributionException {
                InputStream stream = null;
                try {
                    stream = url.openStream();
                    registry.processManifestArtifact(contribution.getManifest(), contentType, stream, context);
                } catch (IOException e) {
                    throw new ContributionException(e);
                } finally {
                    try {
                        if (stream != null) {
                            stream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void iterateArtifacts(Contribution contribution, Action action)
            throws ContributionException {
        File root = FileHelper.toFile(contribution.getLocation());
        assert root.isDirectory();
        iterateArtifactsResursive(contribution, action, root);
    }

    protected void iterateArtifactsResursive(Contribution contribution, Action action, File dir)
            throws ContributionException {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                iterateArtifactsResursive(contribution, action, file);
            } else {
                try {
                    URL entryUrl = file.toURI().toURL();
                    String contentType = contentTypeResolver.getContentType(entryUrl);
                    action.process(contribution, contentType, entryUrl);
                } catch (MalformedURLException e) {
                    throw new ContributionException(e);
                } catch (IOException e) {
                    throw new ContributionException(e);
                } catch (ContentTypeResolutionException e) {
                    throw new ContributionException(e);
                }
            }
        }

    }

}
