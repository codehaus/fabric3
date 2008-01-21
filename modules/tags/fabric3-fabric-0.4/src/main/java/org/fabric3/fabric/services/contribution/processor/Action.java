/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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

import java.net.URL;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.Contribution;

/**
 * Used to perform a callback operation when iterating contained artifacts in a contribution.
 *
 * @version $Rev$ $Date$
 */
public interface Action {
    /**
     * Called when an artifact is reached during iteration.
     *
     * @param contribution the contribution being traversed
     * @param contentType  the artifact MIME type to process
     * @param url          the artifact url
     * @throws ContributionException if an error occurs processing the artifact
     */
    void process(Contribution contribution, String contentType, URL url) throws ContributionException;
}
