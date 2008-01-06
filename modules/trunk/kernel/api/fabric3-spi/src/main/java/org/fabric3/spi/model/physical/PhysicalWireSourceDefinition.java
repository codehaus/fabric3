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

package org.fabric3.spi.model.physical;

import java.net.URI;

import org.fabric3.scdl.ModelObject;
import org.w3c.dom.Document;

/**
 * Represents a physical wire source definition.
 *
 * @version $Revision$ $Date$
 */
public class PhysicalWireSourceDefinition extends ModelObject {

    // URI
    private URI uri;
    private URI callbackUri;
    private boolean conversational;
    private Document key;

    /**
     * Gets the URI.
     *
     * @return the URI.
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Sets the URI.
     *
     * @param uri the URI.
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Gets the callback URI.
     *
     * @return the callback URI.
     */
    public URI getCallbackUri() {
        return callbackUri;
    }

    /**
     * Sets the URI.
     *
     * @param uri the callback URI.
     */
    public void setCallbackUri(URI uri) {
        this.callbackUri = uri;
    }


    public boolean isConversational() {
        return conversational;
    }

    public void setConversational(boolean conversational) {
        this.conversational = conversational;
    }

    /**
     * Gets the key if the wire is for a map based reference.
     * 
     * @return The key if the wire is for a map based reference.
     */
    public Document getKey() {
        return key;
    }

    /**
     * Gets the key if the wire is for a map based reference.
     * 
     * @param key The key if the wire is for a map based reference.
     */
    public void setKey(Document key) {
        this.key = key;
    }
}
