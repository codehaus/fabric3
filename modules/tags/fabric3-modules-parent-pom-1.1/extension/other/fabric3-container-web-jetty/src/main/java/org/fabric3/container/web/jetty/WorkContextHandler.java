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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.handler.HandlerWrapper;

import org.fabric3.container.web.spi.WebRequestTunnel;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;

/**
 * Processes incoming requests for the web application context, adding a WorkContext to the thread so it is associated to user code in the web app.
 *
 * @version $Revision$ $Date$
 */
public class WorkContextHandler extends HandlerWrapper {
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
        WorkContext oldContext = null;
        try {
            WorkContext workContext = new WorkContext();
            CallFrame frame = new CallFrame();
            workContext.addCallFrame(frame);
            oldContext = WorkContextTunnel.setThreadWorkContext(workContext);
            WebRequestTunnel.setRequest(request);
            super.handle(target, request, response, dispatch);
        } finally {
            WorkContextTunnel.setThreadWorkContext(oldContext);
            WebRequestTunnel.setRequest(null);
        }
    }
}
