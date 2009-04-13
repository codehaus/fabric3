/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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

import java.io.Serializable;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Super class for all logical SCA artifacts.
 *
 * @version $Revision$ $Date$
 */
public abstract class LogicalScaArtifact<P extends LogicalScaArtifact<?>> implements Serializable {
    private static final long serialVersionUID = 3937960041374196627L;
    private final URI uri;
    private final P parent;
    private final QName type;
    private Set<QName> intents = new LinkedHashSet<QName>();
    private Set<QName> policySets = new LinkedHashSet<QName>();

    /**
     * @param uri    URI of the SCA artifact.
     * @param parent Parent of the SCA artifact.
     * @param type   Type of this artifact.
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
        if (uri == null) {
            return "";
        }
        return uri.toString();
    }

    public Set<QName> getIntents() {
        return intents;
    }

    public Set<QName> getPolicySets() {
        return policySets;
    }

    public void addIntent(QName intent) {
        intents.add(intent);
    }

    public void addIntents(Set<QName> intents) {
        this.intents.addAll(intents);
    }

    public void addPolicySet(QName policySet) {
        policySets.add(policySet);
    }

    public void removePolicySet(QName policySet) {
        policySets.remove(policySet);
    }

    public void addPolicySets(Set<QName> policySets) {
        this.policySets.addAll(policySets);
    }


}
