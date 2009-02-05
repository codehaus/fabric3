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

import loanapp.api.loan.LoanException;
import loanapp.api.message.Address;
import loanapp.api.message.LoanRequest;
import loanapp.api.message.LoanStatus;
import loanapp.api.request.RequestCoordinator;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.annotations.Context;

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

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // process the application
        RequestCoordinator coordinator = context.getService(RequestCoordinator.class, "requestCoordinator");
        Long id = (Long) req.getSession().getAttribute("loanId");
        String page;
        if (id != null && LoanStatus.NOT_SUBMITTED != coordinator.getStatus(id)) {
            req.setAttribute("loanError", "A loan application has already been submitted");
            page = "/error.jsp";
        } else {
            try {
                LoanRequest request = populateLoanRequest(req);
                id = coordinator.start(request);
                req.getSession().setAttribute("loanId", id);
                page = "/requestSubmitted.jsp";
            } catch (LoanException e) {
                throw new ServletException(e);
            }
        }
        getServletContext().getRequestDispatcher(page).forward(req, resp);
    }

    private LoanRequest populateLoanRequest(HttpServletRequest req) {
        // TODO handle invalid doubles
        LoanRequest request = new LoanRequest();
        request.setSSN(req.getParameter("ssn"));
        request.setEmail(req.getParameter("email"));
        request.setAmount(Double.valueOf(req.getParameter("amount")));
        request.setDownPayment(Double.valueOf(req.getParameter("down")));
        request.setPropertyAddress(populateLocation(req));
        return request;
    }

    private Address populateLocation(HttpServletRequest req) {
        // TODO handle invalid zip
        Address address = new Address();
        address.setStreet(req.getParameter("street"));
        address.setCity(req.getParameter("city"));
        address.setState(req.getParameter("state"));
        // address.setZip(Integer.parseInt(req.getParameter("zip")));
        return address;
    }

}
