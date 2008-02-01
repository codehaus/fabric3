package org.fabric3.security.authorization;

import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;

/**
 * 
 * @version $Revision$ $Date$
 *
 */
public class AuthorizationInterceptorDefinition extends PhysicalInterceptorDefinition {
    
    private String[] roles;
    
    public AuthorizationInterceptorDefinition(String[] roles) {
        this.roles = roles;
    }
    
    public String[] getRoles() {
        return roles;
    }

}
