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
import java.net.URL;


/**
 * Implementations resolve contribution artifacts to a local destination
 *
 * @version $Rev$ $Date$
 */
public interface ArtifactResolver {

    /**
     * Resolves the contribution artifact, returning a local URL where is may be dereferenced
     *
     * @param contributionURI the contribution URI
     * @return the local dereferenceable URL for the artifact
     * @throws ResolutionException if an error occurs resolving the artifact
     */
    URL resolve(URI contributionURI) throws ResolutionException;

}
