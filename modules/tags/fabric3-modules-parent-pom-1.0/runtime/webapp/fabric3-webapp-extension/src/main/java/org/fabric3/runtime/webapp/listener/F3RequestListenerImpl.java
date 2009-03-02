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
package org.fabric3.runtime.webapp.listener;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.fabric3.container.web.spi.WebRequestTunnel;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.runtime.webapp.F3RequestListener;
import org.fabric3.spi.invocation.WorkContext;

/**
 * @version $Revision$ $Date$
 */
public class F3RequestListenerImpl implements F3RequestListener {

    public void onRequestStart(ServletRequestEvent request) {
        WorkContext workContext = new WorkContext();
        WorkContextTunnel.setThreadWorkContext(workContext);
        ServletRequest req = request.getServletRequest();
        if (req instanceof HttpServletRequest) {
            WebRequestTunnel.setRequest(((HttpServletRequest) req));
        }
    }

    public void onRequestEnd(ServletRequestEvent request) {
        WorkContextTunnel.setThreadWorkContext(null);
        WebRequestTunnel.setRequest(null);
    }

}
