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
     * Contribute an artifact to the SCA Domain, placing it in the specified store.
     *
     * @param storeId the id of the store which will hold the contribution artifact
     * @param source  the contribution source
     * @return a URI that uniquely identifies this contribution within the SCA Domain
     * @throws ContributionException if there was a problem with the contribution
     * @throws IOException           if there was a problem reading the stream
     */
    URI contribute(String storeId, ContributionSource source) throws ContributionException, IOException;

    /**
     * Updates a previously contributed artifact if its timestamp and checksum have changed
     *
     * @param source the contribution source
     * @throws ContributionException if an error occurs during the update procecedure, for example, a previuous
     *                               contribution is not found
     * @throws IOException           if an error occurs reading
     */
    void update(ContributionSource source) throws ContributionException, IOException;

    /**
     * Returns true if a contribution for the given URI exists.
     *
     * @param uri the contribution URI
     * @return true if a contribution for the given URI exists
     */
    boolean exists(URI uri);

    /**
     * Returns the contribution timestamp
     *
     * @param uri the contribution URI
     * @return the timestamp or -1 if no contribution was found
     */
    long getContributionTimestamp(URI uri);

    /**
     * Returns a list of deployable URIs in a contribution
     *
     * @param uri the URI of the contribution to search
     * @return a list of deployable URIs in a contribution. If no deployables are found, an empty list is returned.
     * @throws ContributionNotFoundException if a contribution corresponding to the URI is not found
     */
    public List<QName> getDeployables(URI uri) throws ContributionException;

    /**
     * Remove a contribution from the SCA domain
     *
     * @param uri The URI of the contribution
     * @throws ContributionException if there was a problem with the contribution
     */
    void remove(URI uri) throws ContributionException;

    /**
     * Resolve an artifact by QName within the contribution
     *
     * @param <T>            The java type of the artifact such as javax.wsdl.Definition
     * @param uri            The URI of the contribution
     * @param definitionType The java type of the artifact
     * @param name           The name of the artifact
     * @return The resolved artifact
     */
    <T> T resolve(URI uri, Class<T> definitionType, QName name);

    /**
     * Resolve the reference to an artifact by the location URI within the given contribution. Some typical use cases
     * are: <ul> <li>Reference a XML schema using {http://www.w3.org/2001/XMLSchema-instance}schemaLocation or
     * <li>Reference a list of WSDLs using {http://www.w3.org/2004/08/wsdl-instance}wsdlLocation </ul>
     *
     * @param uri         The URI of the contribution
     * @param namespace   The namespace of the artifact. This is for validation purpose. If the namespace is null, then
     *                    no check will be performed.
     * @param locationUri The location URI
     * @param baseURI     The URI of the base artifact where the reference is declared
     * @return The URL of the resolved artifact
     */
    URL resolve(URI uri, String namespace, URI locationUri, URI baseURI);
}
