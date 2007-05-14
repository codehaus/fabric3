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
package org.fabric3.spi.model.type;

/**
 * Represents a runtime resource that is either required or available. A resource is identified by its type, an
 * identifier, and an optional version.
 *
 * @version $Rev$ $Date$
 */
public class ResourceDescription {
    private String type;
    private Object identifier;
    private String version;

    public ResourceDescription(String type, Object identifier) {
        this.type = type;
        this.identifier = identifier;
    }

    public ResourceDescription(String type, Object identifier, String version) {
        this.type = type;
        this.identifier = identifier;
        this.version = version;
    }

    /**
     * Returns the resource type.
     *
     * @return the resource type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the resource identifier.
     *
     * @return the resource identifier
     */
    public Object getIdentifier() {
        return identifier;
    }

    /**
     * Returns the resource version or null if versioning is not required.
     *
     * @return the resource version or null if versioning is not required
     */
    public String getVersion() {
        return version;
    }
}
