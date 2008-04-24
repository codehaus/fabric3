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
import loanapp.loan.LoanService;
import loanapp.message.LoanRequest;
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

    @Reference
    protected LoanService loanService;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        loanService = (LoanService) getServletContext().getAttribute("loanService");
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String amount = req.getParameter("amount");
        String down = req.getParameter("down");
        String ssn = req.getParameter("ssn");
        LoanRequest request = new LoanRequest();
        request.setAmount(Double.valueOf(amount));
        request.setDownPayment(Double.valueOf(down));
        request.setSSN(ssn);
        // invoke the loan application service
        String id = null;
        try {
            id = loanService.apply(request);
        } catch (LoanException e) {
            throw new ServletException(e);
        }
//        req.setAttribute("loanResult", result);
//        getServletContext().getRequestDispatcher("/result.jsp").forward(req, resp);
    }

}
