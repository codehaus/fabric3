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
 */
package org.fabric3.spi.policy;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.model.type.definitions.AbstractDefinition;
import org.fabric3.model.type.definitions.PolicySet;

/**
 * Registry of binding types, implementation types, intents and policy sets within an SCA domain.
 *
 * @version $Revision$ $Date$
 */
public interface PolicyRegistry {

    /**
     * Returns all the definitions of a given type.
     *
     * @param <D>             Definition type.
     * @param definitionClass Definition class.
     * @return All definitions of the given type.
     */
    <D extends AbstractDefinition> Collection<D> getAllDefinitions(Class<D> definitionClass);

    /**
     * Returns the definition of specified type and qualified name.
     *
     * @param <D>             Definition type.
     * @param name            Qualified name of the definition object.
     * @param definitionClass Definition class.
     * @return Requested definition object if available, otherwise null.
     */
    <D extends AbstractDefinition> D getDefinition(QName name, Class<D> definitionClass);

    /**
     * Returns a list of active PolicySets that use external attachment.
     *
     * @return the PolicySets
     */
    List<PolicySet> getExternalAttachmentPolicies();

    /**
     * Activates all the policy definitions in the specified contributions.
     *
     * @param contributionUris The URIs for the contribution.
     * @throws PolicyActivationException If unable to find definition.
     */
    void activateDefinitions(List<URI> contributionUris) throws PolicyActivationException;

}
