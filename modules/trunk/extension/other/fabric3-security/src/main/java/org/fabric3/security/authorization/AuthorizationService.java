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
