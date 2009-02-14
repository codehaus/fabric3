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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.oasisopen.sca.ComponentContext;

import org.fabric3.runtime.webapp.smoketest.model.Employee;

/**
 * @version $Rev$ $Date$
 */
public class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 1532086282614089270L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        String test = request.getParameter("test");
        if ("context".equals(test)) {
            HttpSession session = request.getSession();
            ComponentContext context = (ComponentContext) session.getAttribute("org.oasisopen.sca.ComponentContext");
            if (context == null) {
                response.sendError(500, "Context was not bound");
                return;
            }
            HelloService service = context.getService(HelloService.class, "hello");
            if (!"Hello World".equals(service.getGreeting())) {
                response.sendError(500, "Failed to create HelloService");
                return;
            }

            EmployeeService employeeService = context.getService(EmployeeService.class, "employeeService");
            employeeService.createEmployee(123l, "Barney Rubble");
            Employee employee = employeeService.findEmployee(123L);
            if (employee == null) {
                response.sendError(500, "Failed to persist Employee");
                return;
            }

            out.print("component URI is " + context.getURI());
        } else {
            response.sendError(500, "No test specified");
        }
    }
}
