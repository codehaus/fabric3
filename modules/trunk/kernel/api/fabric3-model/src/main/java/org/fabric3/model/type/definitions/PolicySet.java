/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
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
