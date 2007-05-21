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
package org.fabric3.host.contribution;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;


/**
 * Service interface that manages artifacts contributed to a Fabric3 domain.
 *
 * @version $Rev$ $Date$
 */
public interface ContributionService {
    /**
     * Contribute an artifact to the domain. The type of the contribution is determined by the Content-Type of the
     * resource or, if that is undefined, by some implementation-specific means (such as mapping an extension in the
     * URL's path).
     *
     * @param source   the location of the resource containing the artifact
     * @param checksum the resource checksum
     * @return a URI that uniquely identifies this contribution within the SCA Domain
     * @throws ContributionException if there was a problem with the contribution
     * @throws IOException           if there was a problem reading the resource
     */
    URI contribute(URL source, byte[] checksum) throws ContributionException, IOException;

    /**
     * Contribute an artifact to the SCA Domain.
     *
     * @param sourceUri   an identifier for the source of this contribution
     * @param contentType the content type to process
     * @param checksum    the resource checksum
     * @param stream      a stream containing the resource being contributed; the stream will not be closed but the read
     *                    position after the call is undefined
     * @return a URI that uniquely identifies this contribution within the SCA Domain
     * @throws ContributionException if there was a problem with the contribution
     * @throws IOException           if there was a problem reading the stream
     */
    URI contribute(URI sourceUri, String contentType, byte[] checksum, InputStream stream)
            throws ContributionException, IOException;

    /**
     * Returns a list of deployable URIs in a contribution
     *
     * @param contributionUri the URI of the contribution to search
     * @return a list of deployable URIs in a contribution. If no deployables are found, an empty list is returned.
     * @throws ContributionNotFoundException if a contribution corresponding to the URI is not found
     */
    public List<QName> getDeployables(URI contributionUri) throws ContributionNotFoundException;

    /**
     * Remove a contribution from the SCA domain
     *
     * @param contributionUri The URI of the contribution
     * @throws ContributionException if there was a problem with the contribution
     */
    void remove(URI contributionUri) throws ContributionException;

    /**
     * Resolve an artifact by QName within the contribution
     *
     * @param <T>             The java type of the artifact such as javax.wsdl.Definition
     * @param contributionUri The URI of the contribution
     * @param definitionType  The java type of the artifact
     * @param name            The name of the artifact
     * @return The resolved artifact
     */
    <T> T resolve(URI contributionUri, Class<T> definitionType, QName name);

    /**
     * Resolve the reference to an artifact by the location URI within the given contribution. Some typical use cases
     * are: <ul> <li>Reference a XML schema using {http://www.w3.org/2001/XMLSchema-instance}schemaLocation or
     * <li>Reference a list of WSDLs using {http://www.w3.org/2004/08/wsdl-instance}wsdlLocation </ul>
     *
     * @param contributionUri The URI of the contribution
     * @param namespace       The namespace of the artifact. This is for validation purpose. If the namespace is null,
     *                        then no check will be performed.
     * @param uri             The location URI
     * @param baseURI         The URI of the base artifact where the reference is declared
     * @return The URL of the resolved artifact
     */
    URL resolve(URI contributionUri, String namespace, URI uri, URI baseURI);
}
