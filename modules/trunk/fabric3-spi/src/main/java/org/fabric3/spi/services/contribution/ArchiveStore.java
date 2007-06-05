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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * Implementations store conribution artifacts
 */
public interface ArchiveStore {
    String DEFAULT_STORE = "DefaultStore";

    /**
     * Returns the store id.
     *
     * @return the store id.
     */
    String getId();

    /**
     * Copies a contribution artifact to the store.
     *
     * @param uri    A URI pointing to the contribution being copied to the store
     * @param stream InputStream with the content of the distribution
     * @return a URL pointing to the stored artifact
     * @throws IOException if an error occurs retrieving or storing the contribution artifact
     */
    URL store(URI uri, InputStream stream) throws IOException;

    /**
     * Copy a contribution artifact from the source URL to the store
     *
     * @param contributionUri the URI of contribution artifact to store
     * @param sourceURL       the source URL of the contribution artifact
     * @return a URL pointing to the stored artifact
     * @throws IOException if an error occurs retrieving or storing the contribution artifact
     */
    URL store(URI contributionUri, URL sourceURL) throws IOException;

    /**
     * Look up the artifact URL by URI
     *
     * @param uri The URI of the contribution
     * @return A URL pointing to the contribution artifact or null if the contribution cannot be found
     */
    URL find(URI uri);

    /**
     * Remove a contribution artifact from the store
     *
     * @param uri The URI of the contribution to be removed
     */
    void remove(URI uri);

    /**
     * Get list of URIs for all the contribution artifacts in the store
     *
     * @return A list of contribution URIs
     */
    List<URI> list();
}
