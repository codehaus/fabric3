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

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.scdl.ValidationContext;

/**
 * Responsible for handling the specifics of an archive-based contribution.
 *
 * @version $Revision$ $Date$
 */
public interface ArchiveContributionHandler {

    /**
     * Returns the content type of the contribution that this handler can process
     *
     * @return the content type.
     */

    String getContentType();

    /**
     * Returns true if the implementation can process the contribution archive.
     *
     * @param contribution the contribution
     * @return true if the implementation can process the contribution archive
     */
    boolean canProcess(Contribution contribution);

    /**
     * Processes the manifest
     *
     * @param contribution the contribution
     * @param context      the context to which validation errors and warnings are reported
     * @throws ContributionException if an error occurs processing the manifest
     */
    void processManifest(Contribution contribution, ValidationContext context) throws ContributionException;

    /**
     * Iterates through a contribution calling the supplied action when a contained artifact is encountered.
     *
     * @param contribution the contribution
     * @param action       the action to perform when an artifact is encountered
     * @throws ContributionException if an error occurs processing the manifest
     */
    void iterateArtifacts(Contribution contribution, Action action) throws ContributionException;

}
