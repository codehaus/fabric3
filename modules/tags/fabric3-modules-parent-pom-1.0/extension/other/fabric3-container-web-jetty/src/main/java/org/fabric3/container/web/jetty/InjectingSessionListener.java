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
package org.fabric3.container.web.jetty;

import java.util.List;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.fabric3.spi.ObjectCreationException;
import org.fabric3.pojo.reflection.Injector;

/**
 * Injects reference proxies into an HTTP session when it is created.
 *
 * @version $Revision$ $Date$
 */
public class InjectingSessionListener implements HttpSessionListener {
    private List<Injector<HttpSession>> injectors;

    public InjectingSessionListener(List<Injector<HttpSession>> injectors) {
        this.injectors = injectors;
    }

    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        for (Injector<HttpSession> injector : injectors) {
            try {
                injector.inject(session);
            } catch (ObjectCreationException e) {
                throw new RuntimeInjectionException(e);
            }
        }
    }

    public void sessionDestroyed(HttpSessionEvent se) {

    }
}
