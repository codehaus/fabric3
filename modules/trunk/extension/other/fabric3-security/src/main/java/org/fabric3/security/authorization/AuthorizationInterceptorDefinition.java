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

import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;

/**
 * Interceptor definition for enforcing authorization. This is built from the 
 * underlying policy definition. An example for the policy definition is shown 
 * below,
 * 
 * <f3:authorization roles="ADMIN,USER"/>
 * 
 * where the namespace prefix f3 maps to the url http://fabric3.org/xmlns/sca/2.0-alpha.
 * 
 * @version $Revision$ $Date$
 *
 */
public class AuthorizationInterceptorDefinition extends PhysicalInterceptorDefinition {
    
    private String[] roles;
    
    /**
     * Initializes the roles required to pass through this interceptor.
     * 
     * @param roles Roles required by the subject.
     */
    public AuthorizationInterceptorDefinition(String[] roles) {
        this.roles = roles;
    }
    
    /**
     * Gets the roles required to pass through this interceptor.
     * 
     * @return Roles required by the subject.
     */
    public String[] getRoles() {
        return roles;
    }

}
