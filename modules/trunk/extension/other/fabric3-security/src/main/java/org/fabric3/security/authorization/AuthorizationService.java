package org.fabric3.security.authorization;

import javax.security.auth.Subject;

/**
 * SPI extension point for authorization. The service is required to be 
 * provided by the user of the authorization interceptor.
 * 
 * @version $Revsion$ $Date$
 *
 */
public interface AuthorizationService {
    
    /**
     * Checks whether the specified subject has the requested roles.
     * 
     * @param subject Subject whose roles need to be checked.
     * @param roles Roles that need to be checked for the subject.
     * @return Result indicating the access check.
     */
    AuthorizationResult hasRoles(Subject subject, String[] roles);

}
