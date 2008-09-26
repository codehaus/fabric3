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
package org.fabric3.ftp.server.security;

import java.security.cert.X509Certificate;

/**
 * Interface that abstracts logging in users.
 * 
 * @version $Revision$ $Date$
 */
public interface UserManager {
    
    /**
     * Logins a user using user name and password.
     * 
     * @param user Name of the user.
     * @param password Password for the user.
     * @return True if the user name and password are valid.
     */
    boolean login(String user, String password);
    
    /**
     * Login a user using X509 certificate.
     * 
     * @param certificate Certificate of the logging in user.
     * @return True if the user name and password are valid.
     */
    boolean login(X509Certificate certificate);

}
