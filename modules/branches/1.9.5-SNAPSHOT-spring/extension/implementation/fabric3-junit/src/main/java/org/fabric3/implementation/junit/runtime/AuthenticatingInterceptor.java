/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.implementation.junit.runtime;

import org.oasisopen.sca.ServiceUnavailableException;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.UsernamePasswordToken;
import org.fabric3.spi.wire.Interceptor;

/**
 * Sets a security subject for the current testcase invocation using configuration specified in the JUnit component definition.
 *
 * @version $Rev$ $Date$
 */
public class AuthenticatingInterceptor implements Interceptor {
    private Interceptor next;
    private String username;
    private String password;
    private AuthenticationService authenticationService;

    public AuthenticatingInterceptor(String username, String password, AuthenticationService authenticationService, Interceptor next) {
        this.username = username;
        this.password = password;
        this.authenticationService = authenticationService;
        this.next = next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Interceptor getNext() {
        return next;
    }

    public Message invoke(Message msg) {
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        try {
            SecuritySubject subject = authenticationService.authenticate(token);
            msg.getWorkContext().setSubject(subject);
        } catch (AuthenticationException e) {
            throw new ServiceUnavailableException("Error authenticating", e);
        }
        return next.invoke(msg);
    }

}
