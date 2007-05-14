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

import org.fabric3.host.contribution.ContributionException;

/**
 * The system registry of contribution processors
 *
 * @version $Rev$ $Date$
 */
public interface ContributionProcessorRegistry {
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
    void unregister(String contentType);

    /**
     * Process a contribution or an artifact in the contribution from the input stream.
     *
     * @param contribution The contribution that will be used to hold the results from the processing
     * @param contentType  The type of content to process
     * @param source       The URI for the contribution/artifact
     * @param inputStream  The input stream for the contribution. The stream will not be closed but the read position
     *                     after the call is undefined
     * @throws org.fabric3.host.contribution.ContributionException
     *                             if there was a problem with the contribution
     * @throws java.io.IOException if there was a problem reading the stream
     */
    void processContent(Contribution contribution, String contentType, URI source, InputStream inputStream)
            throws ContributionException, IOException;

}