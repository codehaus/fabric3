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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.services.scanner.ResourceMetaData;

/**
 * The base representation of a deployed contribution
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings({"SerializableHasSerializationMethods"})
public class Contribution implements ResourceMetaData, Serializable {
    private static final long serialVersionUID = 2511879480122631196L;
    private final URI uri;
    private URL location;
    private byte[] checksum;
    private long timestamp;
    private ContributionManifest manifest;
    private Map<QName, Map<Object, CompositeComponentType>> types =
            new HashMap<QName, Map<Object, CompositeComponentType>>();

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
     * Returns the dereferenceable URL for the contribution artifact.
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
     * Adds metadata from a composite component type introspected in the contribution
     *
     * @param implementationType the implementation type the component type is associated with, e.g.
     *                           implementation.java
     * @param key                a key representing the implementation artifact the component type represents, e.g. a
     *                           fully qualified Java class name
     * @param type               the component type
     */
    public void addComponentType(QName implementationType, Object key, CompositeComponentType type) {
        Map<Object, CompositeComponentType> map = types.get(implementationType);
        if (map == null) {
            map = new HashMap<Object, CompositeComponentType>();
            types.put(implementationType, map);
        }
        map.put(key, type);
    }

    public Map<QName, Map<Object, CompositeComponentType>> getComponentTypes() {
        return Collections.unmodifiableMap(types);
    }

    /**
     * Returns the introspected component type for corresponding to the implementation artifact key
     *
     * @param implementationType the component implementation type, e.g. implementation.java
     * @param key                a key representing the implementation artifact the component type represents, e.g. a
     *                           fully qualified Java class name
     * @return the component type or null
     */
    public CompositeComponentType getComponentType(QName implementationType, Object key) {
        Map<Object, CompositeComponentType> map = types.get(implementationType);
        if (map == null) {
            return null;
        }
        return map.get(key);
    }
}
