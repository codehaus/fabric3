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
package loanapp.loanservice;

import org.osoa.sca.annotations.Reference;

import loanapp.calculator.LoanCalculator;
import loanapp.credit.CreditCheckService;
import loanapp.message.LoanApplication;
import loanapp.message.LoanRequest;
import loanapp.message.LoanResult;
import loanapp.risk.RiskAssessmentService;

/**
 * Component implementation that receives a loan application and coordinates with other services to process it.
 *
 * @version $Rev$ $Date$
 */
public class LoanApplicationComponent implements LoanApplicationService {
    private CreditCheckService creditService;
    private RiskAssessmentService riskService;
    private LoanCalculator loanCalculator;

    /**
     * Creates a new instance.
     *
     * @param creditService  the service responsible for collecting the applicant's credit score
     * @param riskService    the service that scores the loan risk
     * @param loanCalculator the service that calculates loan options
     */
    public LoanApplicationComponent(@Reference(name = "creditService")CreditCheckService creditService,
                                    @Reference(name = "riskService")RiskAssessmentService riskService,
                                    @Reference(name = "loanCalculator")LoanCalculator loanCalculator) {
        this.creditService = creditService;
        this.riskService = riskService;
        this.loanCalculator = loanCalculator;
    }

    public LoanResult apply(LoanRequest request) {
        String ssn = request.getSSN();
        // pull the applicant's credit score
        int score = creditService.checkCredit(ssn);
        LoanApplication application = new LoanApplication();
        application.setSSN(ssn);
        application.setAmount(request.getAmount());
        application.setDownPayment(request.getDownPayment());
        application.setCreditScore(score);
        // assess the loan risk
        application = riskService.assessRisk(application);
        // calculate the options
        return loanCalculator.calculateOptions(application);
    }

}
