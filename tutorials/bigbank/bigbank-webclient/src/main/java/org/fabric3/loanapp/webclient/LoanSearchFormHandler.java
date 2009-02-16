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

import loanapp.api.loan.LoanNotFoundException;
import loanapp.api.loan.LoanException;
import loanapp.api.loan.LoanService;
import loanapp.api.message.LoanData;
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
public class LoanSearchFormHandler extends HttpServlet {
    private static final long serialVersionUID = -1918315993336708875L;

    @Context
    protected ComponentContext context;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // process the application
        LoanService service = context.getService(LoanService.class, "loanService");
        String page;
        try {
            long id = Long.parseLong(req.getParameter("loanId"));
            try {
                LoanData options = service.review(id);
                req.setAttribute("loanTerms", options);
                page = "/reviewForm.jsp";
            } catch (LoanNotFoundException e) {
                req.setAttribute("loanError", e.getMessage());
                page = "/error.jsp";
            } catch (LoanException e) {
                throw new ServletException(e);
            }
        } catch (NumberFormatException e) {
            req.setAttribute("loanError", "No loan id submitted");
            page = "/error.jsp";

        }
        getServletContext().getRequestDispatcher(page).forward(req, resp);

    }


}