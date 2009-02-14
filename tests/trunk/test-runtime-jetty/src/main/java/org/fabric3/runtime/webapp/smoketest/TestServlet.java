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
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.oasisopen.sca.ComponentContext;

/**
 * @version $Rev$ $Date$
 */
public class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 7698155043124726164L;

    private ServletContext servletContext;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servletContext = config.getServletContext();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String testName = request.getParameter("test");
        if (testName == null || testName.length() == 0) {
            response.sendError(500, "No test specified");
            return;
        }
        // verify the component context was bound to the session
        HttpSession session = request.getSession();
        ComponentContext context = (ComponentContext) session.getAttribute("org.oasisopen.sca.ComponentContext");
        TestService test = context.getService(TestService.class, testName);
        if (test == null) {
            response.sendError(500, "Unknown test: " + testName);
            return;
        }
        // verify the reference was bound to the servlet context as it is non-conversational
        test = (TestService) servletContext.getAttribute(testName);
        if (test == null) {
            response.sendError(500, "Unknown test: " + testName);
            return;
        }

        test.service(request, response, servletContext);

        CounterService counter = context.getService(CounterService.class, "counter");
        counter.increment();
        if (counter.getCount() != 1) {
            response.sendError(500, "Counter expected to be 1");
            return;
        }
        counter.end();
        if (counter.getCount() != 0) {
            response.sendError(500, "Counter expected to be 0");
            return;
        }
        CounterService sessionCounter = (CounterService) request.getSession().getAttribute("counter");
        if (sessionCounter.getCount() != 0) {
            response.sendError(500, "Session counter expected to be 0");
            return;
        }

    }
}
