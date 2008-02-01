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
