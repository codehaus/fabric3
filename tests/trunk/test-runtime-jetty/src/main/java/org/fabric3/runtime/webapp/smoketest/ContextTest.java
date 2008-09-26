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
package org.fabric3.runtime.webapp.smoketest;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;

import org.osoa.sca.ComponentContext;

import org.fabric3.runtime.webapp.Constants;

/**
 * @version $Rev$ $Date$
 */
public class ContextTest implements TestService {
    public void service(HttpServletRequest request, HttpServletResponse response, ServletContext context) throws IOException, ServletException {
        ComponentContext componentContext = (ComponentContext) request.getSession().getAttribute(Constants.CONTEXT_ATTRIBUTE);
        if (componentContext == null) {
            response.sendError(500, "Context was not bound");
        } else if (!"fabric3://domain/smoketest".equals(componentContext.getURI())) {
            response.sendError(500, "Context was not bound");
        } else {
            PrintWriter out = response.getWriter();
            out.print("OK");
        }
    }
}
