/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.security.authorization;

import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;

/**
 * Interceptor definition for enforcing authorization. This is built from the underlying policy definition. An example for the policy definition is
 * shown below,
 * <p/>
 * <f3-policy:authorization roles="ADMIN,USER"/>
 * <p/>
 * where the namespace prefix f3-policy maps to the uri urn:fabric3.org:policy.
 *
 * @version $Revision$ $Date$
 */
public class AuthorizationInterceptorDefinition extends PhysicalInterceptorDefinition {
    private static final long serialVersionUID = -6234482688280435134L;

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
