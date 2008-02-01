package org.fabric3.security.authorization;

import javax.security.auth.Subject;

/**
 * SPI extension point for authorization.
 * 
 * @version $Revsion$ $Date$
 *
 */
public interface AuthorizationService {
    
    /**
     * @param subject
     * @param roles
     * @return
     */
    boolean hasRoles(Subject subject, String[] roles);

}
