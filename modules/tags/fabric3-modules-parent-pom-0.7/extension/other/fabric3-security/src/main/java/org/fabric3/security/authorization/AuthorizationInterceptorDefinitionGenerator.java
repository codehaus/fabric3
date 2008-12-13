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

import javax.xml.namespace.QName;

import org.fabric3.model.type.service.Operation;
import org.fabric3.spi.Namespaces;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * Generates the physical interceptor definition from the underlying policy infoset. An example 
 * for the policy definition is shown below,
 * 
 * <f3-policy:authorization roles="ADMIN,USER"/>
 * 
 * where the namespace prefix f3-policy maps to the uri urn:fabric3.org:policy.
 * 
 * @version $Revision$ $Date$
 *
 */
@EagerInit
public class AuthorizationInterceptorDefinitionGenerator implements InterceptorDefinitionGenerator {
    
    private static final QName EXTENSION_NAME = new QName(Namespaces.POLICY, "authorization");
    private GeneratorRegistry generatorRegistry;

    /**
     * Initializes the interceptor definition generator registry.
     * 
     * @param generatorRegistry Interceptor definition generator registry.
     */
    public AuthorizationInterceptorDefinitionGenerator(@Reference GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    /**
     * Registers with the intereceptor generator registry.
     */
    @Init
    public void init() {
        generatorRegistry.register(EXTENSION_NAME, this);
    }

    /**
     * Generates the interceptor definition from the underlying policy infoset.
     * 
     * @param policyDefinition Policy set definition.
     * @param generatorContext Generator context.
     * @param operation Operation against which the interceptor is generated.
     * @param logicalBinding Logical binding on the service or reference.
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
