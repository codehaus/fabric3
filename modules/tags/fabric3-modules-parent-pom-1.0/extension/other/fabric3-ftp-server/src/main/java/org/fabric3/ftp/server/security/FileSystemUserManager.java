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
import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.Property;

/**
 * 
 * User manager implementation that reads the credential from the file system.
 *
 * @version $Revision$ $Date$
 */
public class FileSystemUserManager implements UserManager {
    
    private Map<String, String> users = new HashMap<String, String>();
    
    /**
     * Logins a user using user name and password.
     * 
     * @param user Name of the user.
     * @param password Password for the user.
     * @return True if the user name and password are valid.
     */
    public boolean login(String user, String password) {
        return users.containsKey(user) && password.equals(users.get(user));
    }
    
    /**
     * Login a user using X509 certificate.
     * 
     * @param certificate Certificate of the logging in user.
     * @return True if the user name and password are valid.
     */
    public boolean login(X509Certificate certificate) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Sets the users and passwords as a map.
     * 
     * @param users Map of users to passwords.
     */
    @Property
    public void setUsers(Map<String, String> users) {
        this.users = users;
    }

}
