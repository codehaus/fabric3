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
import java.net.URISyntaxException;

/**
 * Implementations encode a contribution URI so that it may be dereferenced remotely in a domain, e.g. over HTTP.
 *
 * @version $Rev$ $Date$
 */
public interface ArtifactLocationEncoder {

    /**
     * Encode the local contribution URL.
     *
     * @param uri the uri to encode
     * @return the encoded URL
     * @throws URISyntaxException if the URI is invalid
     */
    URI encode(URI uri) throws URISyntaxException;

}
