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
package org.fabric3.itest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.runtime.BootConfiguration;

/**
 * @version $Revision$ $Date$
 */
public class ExtensionHelper {

    public ArtifactHelper artifactHelper;

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();

    public ExtensionHelper() throws ParserConfigurationException {
    }

    private List<URL> resolveDependencies(Dependency[] dependencies) throws MojoExecutionException {

        List<URL> urls = new ArrayList<URL>();

        if (dependencies == null) {
            return urls;
        }

        for (Dependency dependency : dependencies) {
            Artifact artifact = artifactHelper.resolve(dependency);
            try {
                urls.add(artifact.getFile().toURI().toURL());
            } catch (MalformedURLException e) {
                throw new AssertionError();
            }
        }

        return urls;

    }

    public void processExtensions(BootConfiguration configuration,
                                  Dependency[] extensions,
                                  Dependency[] features,
                                  Dependency[] userExtensions,
                                  File[] userExtensionsArchives) throws MojoExecutionException {
        List<URL> extensionUrls = resolveDependencies(extensions);

        if (features != null) {
            for (Dependency feature : features) {
                Artifact featureArtifact = artifactHelper.resolve(feature);
                extensionUrls.addAll(processFeatures(featureArtifact.getFile()));
            }
        }
        List<ContributionSource> sources = createContributionSources(extensionUrls);
        configuration.setExtensions(sources);

        List<URL> userExtensionUrls = resolveDependencies(userExtensions);
        // add extensions that are not Maven artifacts
        if (userExtensionsArchives != null) {
            for (File entry : userExtensionsArchives) {
                if (!entry.exists()) {
                    throw new MojoExecutionException("User extension does not exist: " + entry);
                }
                try {
                    userExtensionUrls.add(entry.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new MojoExecutionException("Invalid user extension URL: " + entry, e);
                }
            }
        }
        sources = createContributionSources(userExtensionUrls);
        configuration.setUserExtensions(sources);
    }

    private List<ContributionSource> createContributionSources(List<URL> urls) {
        List<ContributionSource> sources = new ArrayList<ContributionSource>();
        for (URL extensionUrl : urls) {
            try {
                URI uri = extensionUrl.toURI();
                ContributionSource source = new FileContributionSource(uri, extensionUrl, -1, new byte[0]);
                sources.add(source);
            } catch (URISyntaxException e) {
                // should not happen
                throw new IllegalArgumentException(e);
            }
        }
        return sources;
    }

    private List<URL> processFeatures(File featureSetFile) throws MojoExecutionException {

        List<Dependency> dependencies = new LinkedList<Dependency>();

        Document featureSetDoc;

        try {
            featureSetDoc = db.parse(featureSetFile);
        } catch (SAXException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        NodeList extensionList = featureSetDoc.getElementsByTagName("extension");

        for (int i = 0; i < extensionList.getLength(); i++) {

            Element extensionElement = (Element) extensionList.item(i);

            Element artifactIdElement = (Element) extensionElement.getElementsByTagName("artifactId").item(0);
            Element groupIdElement = (Element) extensionElement.getElementsByTagName("groupId").item(0);
            Element versionElement = (Element) extensionElement.getElementsByTagName("version").item(0);

            Dependency extension = new Dependency();
            extension.setArtifactId(artifactIdElement.getTextContent());
            extension.setGroupId(groupIdElement.getTextContent());
            extension.setVersion(versionElement.getTextContent());

            dependencies.add(extension);

        }

        return resolveDependencies(dependencies.toArray(new Dependency[dependencies.size()]));

    }

}
