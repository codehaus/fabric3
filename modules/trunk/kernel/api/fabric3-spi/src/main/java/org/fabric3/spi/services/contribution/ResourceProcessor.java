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

import java.net.URI;
import java.net.URL;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.scdl.ValidationContext;

/**
 * Implmentations process a contribution resource for a MIME type.
 *
 * @version $Rev$ $Date$
 */
public interface ResourceProcessor {

    /**
     * Returns the content type the processor handles
     *
     * @return the content type the processor handles
     */
    String getContentType();

    /**
     * Indexes the resource
     *
     * @param contribution the containing contribution
     * @param url          a dereferenceable url to the resource
     * @throws ContributionException if an error occurs during indexing
     */
    void index(Contribution contribution, URL url) throws ContributionException;

    /**
     * Loads the the Resource
     *
     * @param contributionUri the URI of the active contribution
     * @param resource        the resource to process
     * @param context         the context to which validation errors and warnings are reported
     * @param loader          the classloader contribution the resource must be loaded in @throws ContributionException if an error occurs during
     *                        introspection
     * @throws ContributionException if an error processing the contribution occurs
     */
    void process(URI contributionUri, Resource resource, ValidationContext context, ClassLoader loader) throws ContributionException;

}
