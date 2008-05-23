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
package org.fabric3.spi.services.contribution;

import java.io.InputStream;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.scdl.ValidationContext;

/**
 * Processes an artifact containing manifest information in a contribution archive.
 *
 * @version $Rev$ $Date$
 */
public interface ManifestProcessor {

    /**
     * Returns the content type the processor handles
     *
     * @return the content type the processor handles
     */
    String getContentType();

    /**
     * Processes the input stream for the artifact and update the manifest.
     *
     * @param manifest the manifest to update
     * @param stream   the stream for the artifact
     * @param context  the context to which validation errors and warnings are reported
     * @throws ContributionException if an error occurs processing the stream
     */
    void process(ContributionManifest manifest, InputStream stream, ValidationContext context) throws ContributionException;

}

