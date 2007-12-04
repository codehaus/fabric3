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
package org.fabric3.spi.services.contribution;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.fabric3.host.contribution.ContributionException;

/**
 * The system registry of contribution processors
 *
 * @version $Rev$ $Date$
 */
public interface ProcessorRegistry {
    /**
     * Register a ContributionProcessor using the content type as the key
     *
     * @param processor the processor to registrer
     */
    void register(ContributionProcessor processor);

    /**
     * Unregister a ContributionProcessor for a content type
     *
     * @param contentType the content
     */
    void unregisterContributionProcessor(String contentType);

    /**
     * Register a ManifestProcessor using the content type as the key
     *
     * @param processor the processor to registrer
     */
    void register(ManifestProcessor processor);

    /**
     * Unregister a ManifestProcessor for a content type
     *
     * @param contentType the content
     */
    void unregisterManifestProcessor(String contentType);

    /**
     * Register a ResourceProcessor using the content type as the key
     *
     * @param processor the processor to registrer
     */
    void register(ResourceProcessor processor);

    /**
     * Unregister a ResourceProcessor for a content type
     *
     * @param contentType the content
     */
    void unregisterResourceProcessor(String contentType);

    /**
     * Dispatches to a {@link ContributionProcessor} to process manifest information in a contribution.
     *
     * @param contribution The contribution
     * @throws ContributionException if there was a problem processing the manifest
     */
    void processManifest(Contribution contribution) throws ContributionException;

    /**
     * Dispatches to a {@link ManifestProcessor} to process a manifest artifact contaned in a contribution.
     *
     * @param manifest    the manifest to update
     * @param contentType the artifact MIME type
     * @param inputStream the input stream for the artifact
     * @throws ContributionException if there was a problem processing the artifact
     */
    void processManifestArtifact(ContributionManifest manifest,
                                 String contentType,
                                 InputStream inputStream) throws ContributionException;

    /**
     * Dispatches to a {@link ContributionProcessor} to index a contribution.
     *
     * @param contribution the contribution to index
     * @throws ContributionException if there was a problem indexing the contribution
     */
    void indexContribution(Contribution contribution) throws ContributionException;

    /**
     * Dispatches to a {@link ResourceProcessor} to index a resource contained in a contribution.
     *
     * @param contribution the cntaining contribution
     * @param contentType  the content type of the resource to process
     * @param url          a dereferenceable URL for the resource
     * @throws ContributionException if there was a problem indexing the contribution
     */
    void indexResource(Contribution contribution, String contentType, URL url) throws ContributionException;

    /**
     * Loads all indexed resources in a contribution.
     *
     * @param contribution The contribution
     * @param loader       the classloader conribution resources must be laoded in
     * @throws ContributionException if there was a problem loading resources in the contribution
     */
    void processContribution(Contribution contribution, ClassLoader loader) throws ContributionException;

    /**
     * Loads a contained resource in a contribution.
     *
     * @param contributionUri the URI of the active contribution
     * @param resource        the resource to process
     * @param loader          the classloader contribution the resource must be loaded in
     * @throws ContributionException if there was a problem loading the resoure
     */
    void processResource(URI contributionUri, Resource resource, ClassLoader loader) throws ContributionException;

}
