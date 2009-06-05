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

/**
 * Represents a connected user.
 * 
 * @version $Revision$ $Date$
 */
public class User {
    
    private final String name;
    private boolean authenticated;

    /**
     * Initializes the user name.
     * 
     * @param name Name of the user.
     */
    public User(String name) {
        this.name = name;
    }
    
    /**
     * Checks whether the user is authenticated.
     * 
     * @return True if the user is authenticated.
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Sets the user as authenticated.
     */
    public void setAuthenticated() {
        this.authenticated = true;
    }

    /**
     * Gets the name of the logged on user.
     * 
     * @return Name of the logged on user.
     */
    public String getName() {
        return name;
    }

}
