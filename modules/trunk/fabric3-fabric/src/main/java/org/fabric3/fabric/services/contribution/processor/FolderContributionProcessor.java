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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.extension.contribution.ContributionProcessorExtension;
import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionProcessor;

public class FolderContributionProcessor extends ContributionProcessorExtension implements ContributionProcessor {

    public String getContentType() {
        return Constants.FOLDER_CONTENT_TYPE;
    }

    public void processContent(Contribution contribution, URI source, InputStream inputStream)
            throws ContributionException, IOException {
//        URL contributionURL = contribution.getArtifact(source).getLocation();
//
//        for (URL artifactURL : getArtifacts(contributionURL)) {
//            String artifactPath = artifactURL.toExternalForm().substring(contributionURL.toExternalForm().length());
//            URI artifactURI = contribution.getUri().resolve(artifactPath);
//            ContributedArtifact artifact = new ContributedArtifact(artifactURI);
//            artifact.setLocation(artifactURL);
//            contribution.addArtifact(artifact);
//
//            // just process scdl and contribution metadata for now
//            if (ContentType.COMPOSITE.equals(contentType)) {
//                InputStream is = artifactURL.openStream();
//                try {
//                    this.registry.processContent(contribution, CONTENT_TYPE, artifactURI, is);
//                } finally {
//                    IOHelper.closeQuietly(is);
//                }
//            }
//        }
    }

    /**
     * Recursively traverse a root directory
     *
     * @param fileList
     * @param root
     * @throws IOException
     */
    private void traverse(List<URL> fileList, File root) throws IOException {
        if (root.isFile()) {
            fileList.add(root.toURL());
        } else if (root.isDirectory()) {
            // FIXME: Maybe we should externalize it as a property
            // Regular expression to exclude .xxx files
            File[] files = root.listFiles(FileHelper.getFileFilter("[^\u002e].*", true));
            for (File file : files) {
                traverse(fileList, file);
            }
        }
    }

    /**
     * Get a list of files from the directory
     *
     * @param rootURL
     * @return
     * @throws IOException
     * @throws ContributionException
     */
    private List<URL> getArtifacts(URL rootURL) throws ContributionException,
            IOException {
        List<URL> artifacts = new ArrayList<URL>();

        // Assume the root is a jar file
        File rootFolder;

        try {
            rootFolder = new File(rootURL.toURI());
            if (rootFolder.isDirectory()) {
                this.traverse(artifacts, rootFolder);
            }

        } catch (URISyntaxException e) {
            throw new InvalidFolderContributionURIException(rootURL.toExternalForm(), e);
        }

        return artifacts;
    }


}