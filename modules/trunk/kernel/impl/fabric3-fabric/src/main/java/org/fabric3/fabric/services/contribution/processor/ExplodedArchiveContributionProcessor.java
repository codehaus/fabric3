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
import org.fabric3.loader.common.IntrospectionContextImpl;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.Loader;
import org.fabric3.spi.services.contenttype.ContentTypeResolutionException;
import org.fabric3.spi.services.contenttype.ContentTypeResolver;
import org.fabric3.spi.services.contribution.ArtifactLocationEncoder;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;

/**
 * Handles exploded archives on a filesystem.
 */
public class ExplodedArchiveContributionProcessor extends ArchiveContributionProcessor {
    private Loader loader;
    private final ContentTypeResolver contentTypeResolver;

    public ExplodedArchiveContributionProcessor(@Reference Loader loader,
                                                @Reference ContentTypeResolver contentTypeResolver,
                                                @Reference ArtifactLocationEncoder encoder) {
        super(encoder);
        this.loader = loader;
        this.contentTypeResolver = contentTypeResolver;
    }

    public String[] getContentTypes() {
        return new String[]{Constants.FOLDER_CONTENT_TYPE};
    }

    public void processManifest(Contribution contribution) throws ContributionException {
        ContributionManifest manifest;

        try {
            URL sourceUrl = contribution.getLocation();
            URL manifestUrl = new URL(sourceUrl.toString() + "/META-INF/sca-contribution.xml");
            ClassLoader cl = getClass().getClassLoader();
            URI uri = contribution.getUri();
            IntrospectionContext context = new IntrospectionContextImpl(cl, uri, null);
            manifest = loader.load(manifestUrl, ContributionManifest.class, context);
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
                    registry.processManifestArtifact(contribution.getManifest(), contentType, stream);
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

    protected void iterateArtifacts(Contribution contribution, Action action)
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
