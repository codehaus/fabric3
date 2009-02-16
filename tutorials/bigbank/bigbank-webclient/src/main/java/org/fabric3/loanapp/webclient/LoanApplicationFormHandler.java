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
import loanapp.api.loan.LoanService;
import loanapp.api.message.Address;
import loanapp.api.message.LoanRequest;
import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.annotation.Context;

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
        LoanService service = context.getService(LoanService.class, "loanService");
        try {
            LoanRequest request = populateLoanRequest(req);
            long id = service.apply(request);
            req.getSession().setAttribute("loanId", id);
            String page = "/requestSubmitted.jsp";
            getServletContext().getRequestDispatcher(page).forward(req, resp);
        } catch (LoanException e) {
            throw new ServletException(e);
        }
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
