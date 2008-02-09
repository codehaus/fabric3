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

import org.w3c.dom.Document;

import org.fabric3.scdl.ModelObject;
import org.fabric3.spi.model.instance.ValueSource;

/**
 * Represents a physical wire source definition.
 *
 * @version $Revision$ $Date$
 */
public class PhysicalWireSourceDefinition extends ModelObject {

    private URI uri;
    private ValueSource valueSource;
    private URI callbackUri;
    private boolean optimizable;
    private boolean conversational;
    private Document key;

    /**
     * Returns the URI of the physical component that is the source of invocations on this wire.
     *
     * @return the URI of the physical component that is the source of invocations on this wire
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Sets the URI of the physical component that is the source of invocations on this wire.
     *
     * @param uri the URI of the physical component that is the source of invocations on this wire
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the value source for this wire. This identifies which reference or resource on the component this wire applies to.
     * @return the value source for this wire
     */
    public ValueSource getValueSource() {
        return valueSource;
    }

    /**
     * Sets the value source for this wire.
     * @param valueSource the value source for this wire
     */
    public void setValueSource(ValueSource valueSource) {
        this.valueSource = valueSource;
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
     * Sets the callback URI.
     *
     * @param uri the callback URI.
     */
    public void setCallbackUri(URI uri) {
        this.callbackUri = uri;
    }


    /**
     * Returns whether the source side of the wire is optimizable.
     *
     * @return true if the source side of the wire is optimizable
     */
    public boolean isOptimizable() {
        return optimizable;
    }

    /**
     * Sets whether the source side of the wire is optimizable.
     *
     * @param optimizable whether the source side of the wire is optimizable
     */
    public void setOptimizable(boolean optimizable) {
        this.optimizable = optimizable;
    }

    /**
     * Returns whether the service contract for this wire is conversational.
     * <p/>
     * If needed, this should be moved up to the PhysicalWireDefinition as it applies to both source and target.
     *
     * @return true if the service contract is conversational
     */
    @Deprecated
    public boolean isConversational() {
        return conversational;
    }

    /**
     * Sets whether the service contract for this wire is conversational.
     *
     * @param conversational true if the service contract is conversational
     */
    @Deprecated
    public void setConversational(boolean conversational) {
        this.conversational = conversational;
    }

    /**
     * Returns the key to be used when this wire is part of a Map reference.
     *
     * @return the key to be used when this wire is part of a Map reference
     */
    public Document getKey() {
        return key;
    }

    /**
     * Sets the key to be used when this wire is part of a Map reference.
     *
     * @param key the key to be used when this wire is part of a Map reference
     */
    public void setKey(Document key) {
        this.key = key;
    }
}
