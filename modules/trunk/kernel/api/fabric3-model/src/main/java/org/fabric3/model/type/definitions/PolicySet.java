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
package org.fabric3.model.type.definitions;

import java.net.URI;
import java.util.Set;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Model object that represents a policy set.
 *
 * @version $Revision$ $Date$
 */
public final class PolicySet extends AbstractDefinition {
    private static final long serialVersionUID = -4507145141780962741L;

    /**
     * Intents provided by this policy set.
     */
    private final Set<QName> provides;
    private URI contributionUri;

    /**
     * Policy set extension
     */
    private final Element extension;

    /**
     * XPath expression for the apples to attribute.
     */
    private final String appliesTo;

    /**
     * XPath expression for the attach to attribute.
     */
    private final String attachTo;

    /**
     * The phase at which the policy is applied.
     */
    private final PolicyPhase phase;

    /**
     * Initializes the state for the policy set.
     *
     * @param name            Name of the policy set.
     * @param provides        Intents provided by this policy set.
     * @param appliesTo       XPath expression for the applies to attribute.
     * @param attachTo        XPath expression for the attach to attribute.
     * @param extension       Extension for the policy set.
     * @param phase           The phase at which the policy is applied.
     * @param contributionUri the contribution this policy set is contained in
     */
    public PolicySet(QName name, Set<QName> provides, String appliesTo, String attachTo, Element extension, PolicyPhase phase, URI contributionUri) {
        super(name);
        this.provides = provides;
        this.attachTo = "".equals(attachTo) ? null : attachTo;
        this.appliesTo = "".equals(appliesTo) ? null : appliesTo;
        this.extension = extension;
        this.phase = phase;
        this.contributionUri = contributionUri;
    }

    /**
     * XPath expression to the element to which the policy set applies.
     *
     * @return The apples to XPath expression.
     */
    public String getAppliesTo() {
        return appliesTo;
    }

    /**
     * XPath expression to the element to which the policy set attaches.
     *
     * @return The attaches to XPath expression.
     */
    public String getAttachTo() {
        return attachTo;
    }

    /**
     * Checks whether the specified intent is provided by this policy set.
     *
     * @param intent Intent that needs to be checked.
     * @return True if this policy set provides to the specified intent.
     */
    public boolean doesProvide(QName intent) {
        return provides.contains(intent);
    }

    /**
     * Checks whether the specified intents is provided by this policy set.
     *
     * @param intents Intents that need to be checked.
     * @return True if this policy set provides to the specified intent.
     */
    public boolean doesProvide(Set<QName> intents) {
        return provides.containsAll(intents);
    }

    /**
     * @return Extension for the policy set.
     */
    public Element getExtension() {
        return extension;
    }

    /**
     * @return Qualified name of the extension element.
     */
    public QName getExtensionName() {
        return new QName(extension.getNamespaceURI(), extension.getLocalName());
    }

    /**
     * @return Gets the policy phase.
     */
    public PolicyPhase getPhase() {
        return phase;
    }

    /**
     * @return Gets the contribution this policy set is contained in.
     */
    public URI getContributionUri() {
        return contributionUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PolicySet policySet = (PolicySet) o;

        if (appliesTo != null ? !appliesTo.equals(policySet.appliesTo) : policySet.appliesTo != null) return false;
        if (attachTo != null ? !attachTo.equals(policySet.attachTo) : policySet.attachTo != null) return false;
        if (contributionUri != null ? !contributionUri.equals(policySet.contributionUri) : policySet.contributionUri != null) return false;
        if (extension != null ? !extension.equals(policySet.extension) : policySet.extension != null) return false;
        if (phase != policySet.phase) return false;
        if (provides != null ? !provides.equals(policySet.provides) : policySet.provides != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (provides != null ? provides.hashCode() : 0);
        result = 31 * result + (contributionUri != null ? contributionUri.hashCode() : 0);
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        result = 31 * result + (appliesTo != null ? appliesTo.hashCode() : 0);
        result = 31 * result + (attachTo != null ? attachTo.hashCode() : 0);
        result = 31 * result + (phase != null ? phase.hashCode() : 0);
        return result;
    }
}
