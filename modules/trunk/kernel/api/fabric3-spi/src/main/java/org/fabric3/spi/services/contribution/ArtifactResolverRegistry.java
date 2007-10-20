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


/**
 * A registry for artifact resolvers. Artifact resolvers are responsible for dereferencing remote resource URLs for a
 * given scheme/protocol and returning a URL where they can be accessed locally. For example, a contribution in an SCA
 * Domain may be available from a remote archive over HTTP. An HTTP artifact resolver would be responsible for obtaining
 * the contribution, caching it, and returning a local URL for accessing it.
 *
 * @version $Rev$ $Date$
 */
public interface ArtifactResolverRegistry extends ArtifactResolver {
    /**
     * Register a resolver by resolution scheme
     *
     * @param scheme   the resolution scheme
     * @param resolver The resolver
     */
    void register(String scheme, ArtifactResolver resolver);

    /**
     * Deregister a resolver that was registered for the given scheme
     *
     * @param scheme the scheme to deregister
     */
    void unregister(String scheme);
}
