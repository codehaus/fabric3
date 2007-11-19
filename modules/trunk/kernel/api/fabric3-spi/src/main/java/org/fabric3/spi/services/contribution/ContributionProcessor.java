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

import java.net.URI;

import org.fabric3.host.contribution.ContributionException;

/**
 * Interface for services that process contributions.
 *
 * @version $Rev$ $Date$
 */
public interface ContributionProcessor {
    /**
     * Returns the content type this implementation handles.
     *
     * @return the content type this implementation handles
     */
    public abstract String[] getContentTypes();

    /**
     * Processses manifest information for the contribution, including imports and exports.
     *
     * @param contribution The contribution that will be used to hold the results from the processing
     * @throws ContributionException if there was a problem with the contribution
     */
    void processManifest(Contribution contribution) throws ContributionException;

    /**
     * Process a contribution or an artifact in the contribution from the input stream. The processor might add
     * artifacts or model objects to the contribution object.
     *
     * @param contribution The contribution that will be used to hold the results from the processing
     * @param source       The URI for the contribution/artifact
     * @throws ContributionException if there was a problem with the contribution
     */
    void processContent(Contribution contribution, URI source) throws ContributionException;

}
