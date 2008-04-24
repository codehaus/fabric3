/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.loanapp.webclient;

import loanapp.loan.LoanException;
import loanapp.message.Address;
import loanapp.message.LoanRequest;
import loanapp.message.LoanStatus;
import loanapp.message.PropertyLocation;
import loanapp.request.RequestCoordinator;
import loanapp.validation.ValidationService;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.annotations.Context;
import org.osoa.sca.annotations.Reference;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
public class LoanApplicationFormHandler extends HttpServlet {
    private static final long serialVersionUID = -1918315993336708875L;

    @Context
    protected ComponentContext context;

    @Reference
    protected ValidationService validationService;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // process the application
        RequestCoordinator coordinator = context.getService(RequestCoordinator.class, "requestCoordinator");
        String id = (String) req.getSession().getAttribute("loanId");
        if (id != null && LoanStatus.NOT_SUBMITTED != coordinator.getStatus(id)) {
            // TODO send already submitted page
            resp.getWriter().write("<html><body>Already submitted</body></html>");
            return;
        }
        try {
            LoanRequest request = populateLoanRequest(req);
            id = coordinator.start(request);
            req.getSession().setAttribute("loanId", id);
            resp.getWriter().write("<html><body>Loan request submitted: " + id + " </body></html>");
        } catch (LoanException e) {
            throw new ServletException(e);
        }
//        req.setAttribute("loanResult", result);
//        getServletContext().getRequestDispatcher("/result.jsp").forward(req, resp);
    }

    private LoanRequest populateLoanRequest(HttpServletRequest req) {
        // TODO handle invalid doubles
        LoanRequest request = new LoanRequest();
        request.setSSN(req.getParameter("ssn"));
        request.setEmail(req.getParameter("email"));
        request.setAmount(Double.valueOf(req.getParameter("amount")));
        request.setDownPayment(Double.valueOf(req.getParameter("down")));
        request.setPropertyLocation(populateLocation(req));
        return request;
    }

    private PropertyLocation populateLocation(HttpServletRequest req) {
        // TODO handle invalid zip
        PropertyLocation location = new PropertyLocation();
        Address address = new Address();
        address.setStreet(req.getParameter("street"));
        address.setCity(req.getParameter("city"));
        address.setState(req.getParameter("state"));
        // address.setZip(Integer.parseInt(req.getParameter("zip")));
        location.setAddress(address);
        return null;
    }

}
