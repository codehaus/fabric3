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
package org.fabric3.security.authorization;

import org.osoa.sca.annotations.EagerInit;
import org.w3c.dom.Element;

import org.fabric3.model.type.service.Operation;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;

/**
 * Generates the physical interceptor definition from the underlying policy infoset. An example for the policy definition is shown below,
 * <p/>
 * <f3-policy:authorization roles="ADMIN,USER"/>
 * <p/>
 * where the namespace prefix f3-policy maps to the uri urn:fabric3.org:policy.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class AuthorizationInterceptorDefinitionGenerator implements InterceptorDefinitionGenerator {

    /**
     * Generates the interceptor definition from the underlying policy infoset.
     *
     * @param policyDefinition Policy set definition.
     * @param operation        Operation against which the interceptor is generated.
     * @param logicalBinding   Logical binding on the service or reference.
     * @return Physical interceptor definition.
     */
    public AuthorizationInterceptorDefinition generate(Element policyDefinition,
                                                       Operation<?> operation,
                                                       LogicalBinding<?> logicalBinding) {

        String rolesAttribute = policyDefinition.getAttribute("roles");
        if (rolesAttribute == null) {
            throw new AssertionError("No roles are defined in the authorization policy");
        }
        String[] roles = rolesAttribute.split(",");

        return new AuthorizationInterceptorDefinition(roles);

    }

}
