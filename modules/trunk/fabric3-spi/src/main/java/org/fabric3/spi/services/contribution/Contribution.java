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

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.fabric3.spi.model.type.ModelObject;

/**
 * The base representation of a deployed contribution
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings({"SerializableHasSerializationMethods"})
public class Contribution implements Serializable {
    private static final long serialVersionUID = 2511879480122631196L;
    private final URI uri;
    private URL location;
    private byte[] checksum;
    private long timestamp;
    private ContributionManifest manifest;
    private Map<QName, ModelObject> types = new HashMap<QName, ModelObject>();
    private List<URI> resolvedImports = new ArrayList<URI>();

    public Contribution(URI uri) {
        this.uri = uri;
    }

    /**
     * Instantiates a new Contribution instance
     *
     * @param uri       the contribution URI
     * @param location  a dereferenceble URL for the contribution archive
     * @param checksum  the checksum for the contribution artifact
     * @param timestamp the time stamp of the contribution artifact
     */
    public Contribution(URI uri, URL location, byte[] checksum, long timestamp) {
        this.uri = uri;
        this.location = location;
        this.checksum = checksum;
        this.timestamp = timestamp;
    }

    /**
     * Returns the contribution URI.
     *
     * @return the contribution URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the locally dereferenceable URL for the contribution artifact.
     *
     * @return the dereferenceable URL for the contribution artifact
     */
    public URL getLocation() {
        return location;
    }

    /**
     * Returns the contribution artifact checksum.
     *
     * @return the contribution artifact checksum
     */
    public byte[] getChecksum() {
        return checksum;
    }

    /**
     * Returns the timestamp of the most recent update to the artifact.
     *
     * @return the timestamp of the most recent update to the artifact
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the contribution manifest
     *
     * @return the contribution manifest
     */
    public ContributionManifest getManifest() {
        return manifest;
    }

    /**
     * Sets the contribution manifest.
     *
     * @param manifest the contribution manifest
     */
    public void setManifest(ContributionManifest manifest) {
        this.manifest = manifest;
    }

    /**
     * Adds metadata from a referenceable type introspected in the contribution
     *
     * @param name the type's qualified name
     * @param type the component type
     */
    public void addType(QName name, ModelObject type) {
        types.put(name, type);
    }

    /**
     * Returns the referenceable introspected types by QName for the contribution
     *
     * @return the introspected component types
     */
    public Map<QName, ModelObject> getTypes() {
        return Collections.unmodifiableMap(types);
    }

    /**
     * Returns the referenceable introspected type for corresponding to the QName key
     *
     * @param key the component type QName
     * @return the type or null
     */
    public ModelObject getType(QName key) {
        return types.get(key);
    }

    /**
     * Adds a contribution URI for a resolved import.
     *
     * @param uri the contribution URI
     */
    public void addResolvedImportUri(URI uri) {
        resolvedImports.add(uri);
    }

    /**
     * Returns the list of contribution URIs matching resolved imports.
     *
     * @return the list of contribution URIs matching resolved imports
     */
    public List<URI> getResolvedImportUris() {
        return Collections.unmodifiableList(resolvedImports);
    }
}
