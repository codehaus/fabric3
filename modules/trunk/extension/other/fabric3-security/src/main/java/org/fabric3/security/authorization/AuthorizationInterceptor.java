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

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;

/**
 * Interceptor for performing role based authorization.
 *
 * @version $Rev$ $Date$
 */
public class AuthorizationInterceptor implements Interceptor {

    private Interceptor next;
    private final String[] roles;
    private final AuthorizationService authorizationService;

    /**
     * Initializes the roles required to pass through this interceptor and the user provided SPI extension for performing authorization.
     *
     * @param roles                Roles that need to be checked by this instance of the interceptor.
     * @param authorizationService AUthorization service extension to perform authorization.
     */
    public AuthorizationInterceptor(String[] roles, AuthorizationService authorizationService) {
        this.roles = roles;
        this.authorizationService = authorizationService;
    }

    /**
     * Gets the next interceptor in the chain.
     *
     * @return The next interceptor in the chain.
     */
    public Interceptor getNext() {
        return next;
    }

    /**
     * Sets the next interceptor in the chain.
     *
     * @param next The next interceptor in the chain.
     */
    public void setNext(Interceptor next) {
        this.next = next;
    }

    /**
     * Performs the authorization check. If succesful the next interceptor in the chain is invoked. If authorization fails, a fault is set on the
     * message and returned to the preceding intereceptor in the chain.
     *
     * @param msg Message passed in by the preceding interceptor.
     */
    public Message invoke(Message msg) {

        WorkContext workContext = msg.getWorkContext();

        AuthorizationResult result = authorizationService.hasRoles(workContext.getSubject(), roles);
        if (result.isSuccess()) {
            return next.invoke(msg);
        } else {
            msg.setBodyWithFault(result.getFault());
            return msg;
        }

    }

}
