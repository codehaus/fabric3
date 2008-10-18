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
package org.fabric3.admin.impl;

import java.net.URL;

import org.fabric3.admin.api.AdministrationException;
import org.fabric3.admin.api.DomainController;

/**
 * Default implementation of the DomainController API.
 *
 * @version $Revision$ $Date$
 */
public class DomainControllerImpl implements DomainController {
    private String username;
    private String password;

    public void setControllerAddress(String domain) {

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void install(URL contribution, String name) throws AdministrationException {

    }

    public void deploy(String name) throws AdministrationException {

    }
}
