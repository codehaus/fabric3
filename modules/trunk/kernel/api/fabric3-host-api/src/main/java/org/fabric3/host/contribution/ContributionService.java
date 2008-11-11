/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.host.contribution;

import java.net.URI;
import java.util.List;
import java.util.Set;


/**
 * Manages artifacts contributed to a domain. Contributions can be application or extension artifacts. Contributions may be in a variety of formats,
 * for example, a JAR or XML document.
 * <p/>
 * The lifecycle of a contribution is defined as follows:
 * <pre>
 * <ul>
 * <li>Stored - The contribution artifact is persisted.
 * <li>Installed - The contribution is introspected, validated, and loaded.
 * <li>Uninstalled - The contribution is unloaded.
 * <li>Removed - the contribution is removed from persistent storage.
 * </ul>
 *
 * @version $Rev$ $Date$
 */
public interface ContributionService {

    /**
     * Persistently stores a contribution in the domain.
     *
     * @param source the contribution source
     * @return a URI that uniquely identifies this contribution within the domain
     * @throws StoreException if there is an error storing the contribution
     */
    URI store(ContributionSource source) throws StoreException;

    /**
     * Installs a stored contribution.
     *
     * @param uri the contribution URI
     * @throws InstallException              if there an error reading, introspecting or loading the contribution
     * @throws ContributionNotFoundException if the contribution is not found
     */
    void install(URI uri) throws InstallException, ContributionNotFoundException;

    /**
     * Installs a list of stored contributions.
     *
     * @param uris the contribution URIs
     * @throws ContributionNotFoundException if a contribution is not found
     * @throws InstallException              if there an error reading, introspecting or loading the contribution
     */
    void install(List<URI> uris) throws InstallException, ContributionNotFoundException;


    /**
     * Stores and installs an artifact.
     *
     * @param source the contribution source
     * @return a URI that uniquely identifies this contribution within the domain
     * @throws ContributionException if there an error reading, introspecting or loading the contribution
     */
    URI contribute(ContributionSource source) throws ContributionException;

    /**
     * Stores and installs a collection of artifacts to a domain. Artifacts will be ordered by import dependencies.
     *
     * @param sources the artifacts to contribute
     * @return a list of contributed URIs.
     * @throws ContributionException if an error is encountered contributing the artifacts
     */
    List<URI> contribute(List<ContributionSource> sources) throws ContributionException;

    /**
     * Updates a previously stored artifact if its timestamp and checksum have changed.
     *
     * @param source the contribution source
     * @throws ContributionException if an error occurs during the update procecedure, for example, a previuous contribution is not found
     */
    void update(ContributionSource source) throws ContributionException;

    /**
     * Uninstalls a contribution.
     *
     * @param uri The URI of the contribution
     * @throws ContributionException if there was a problem with the contribution
     */
    void uninstall(URI uri) throws ContributionException;

    /**
     * Remove a contribution from persistent storage. Contribution must be uninstalled prior to being removed.
     *
     * @param uri The URI of the contribution
     * @throws ContributionException if there was a problem with the contribution
     */
    void remove(URI uri) throws ContributionException;

    /**
     * Returns true if a contribution for the given URI exists.
     *
     * @param uri the contribution URI
     * @return true if a contribution for the given URI exists
     */
    boolean exists(URI uri);

    /**
     * Returns the contribution timestamp.
     *
     * @param uri the contribution URI
     * @return the timestamp or -1 if no contribution was found
     */
    long getContributionTimestamp(URI uri);

    /**
     * Returns the URIs of contributions in the domain.
     *
     * @return the URIs of contributions in the domain
     */
    public Set<URI> getContributions();

    /**
     * Returns a list of deployables in a contribution.
     *
     * @param uri the URI of the contribution to search
     * @return a list of deployables in a contribution. If no deployables are found, an empty list is returned.
     * @throws ContributionNotFoundException if a contribution corresponding to the URI is not found
     */
    public List<Deployable> getDeployables(URI uri) throws ContributionNotFoundException;

}
