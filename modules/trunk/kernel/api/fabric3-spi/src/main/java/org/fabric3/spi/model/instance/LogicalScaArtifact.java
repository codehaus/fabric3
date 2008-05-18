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
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * Super class for all logical SCA artifacts.
 *
 * @version $Revision$ $Date$
 */
public abstract class LogicalScaArtifact<P extends LogicalScaArtifact<?>> {
    private final URI uri;
    private final P parent;
    private final QName type;

    /**
     * @param uri URI of the SCA artifact.
     * @param parent Parent of the SCA artifact.
     * @param type Type of this artifact.
     */
    public LogicalScaArtifact(final URI uri, final P parent, final QName type) {
        this.uri = uri;
        this.parent = parent;
        this.type = type;
    }

    /**
     * Returns the uri.
     *
     * @return the uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * @return Type of this SCA artifact.
     */
    public QName getType() {
        return type;
    }
    
    /**
     * @return Parent of this SCA artifact.
     */
    public final P getParent() {
        return parent;
    }

    public String toString() {
        return uri.toString();
    }

    /**
     * @return Intents declared on the SCA artifact.
     */
    public abstract Set<QName> getIntents();
    
    /**
     * @param intents Intents declared on the SCA artifact.
     */
    public abstract void setIntents(Set<QName> intents);

    /**
     * @return Policy sets declared on the SCA artifact.
     */
    public abstract Set<QName> getPolicySets() ;

    /**
     * @param policySets Policy sets declared on the SCA artifact.
     */
    public abstract void setPolicySets(Set<QName> policySets) ;
    
}
