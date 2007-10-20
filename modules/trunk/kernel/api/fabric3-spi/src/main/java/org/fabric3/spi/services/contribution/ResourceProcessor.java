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

/**
 * Introspects a given stream and returns a Resource based on its contents.
 *
 * @version $Rev$ $Date$
 */
public interface ResourceProcessor {

    /**
     * Performs the introspection and returns a Resource
     *
     * @param stream the stream to introspect
     * @return the resource
     * @throws ContributionException if an error occurs during introspection
     */
    Resource process(InputStream stream) throws ContributionException;

    /**
     * Returns the content type the processor handles
     *
     * @return the content type the processor handles
     */
    String getContentType();
}
