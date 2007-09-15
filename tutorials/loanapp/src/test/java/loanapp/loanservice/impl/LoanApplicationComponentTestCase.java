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
package loanapp.loanservice.impl;

import junit.framework.TestCase;
import loanapp.calculator.LoanCalculator;
import loanapp.credit.CreditCheckService;
import loanapp.loanservice.LoanApplicationComponent;
import loanapp.message.LoanApplication;
import loanapp.message.LoanRequest;
import loanapp.message.LoanResult;
import loanapp.risk.RiskAssessmentService;

/**
 * Demonstrates unit testing outside the Fabric3 container. For automated mock object creation and verification,
 * EasyMock (www.easymock.org) is recommended.
 *
 * @version $Rev$ $Date$
 */
public class LoanApplicationComponentTestCase extends TestCase {
    private LoanApplicationComponent loanService;

    /**
     * Verifies the loan application component can receive and application and approve it.
     */
    public void testLoanApplication() {
        LoanRequest request = new LoanRequest();
        request.setSSN("111-11-1111");
        request.setAmount(100000);
        request.setDownPayment(10000);
        LoanResult result = loanService.apply(request);
        assertTrue(LoanResult.DECLINED != result.getResult());
    }

    /**
     * Test fixture
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        super.setUp();
        CreditCheckService checkService = new CreditCheckServiceMock();
        LoanCalculator calculatorService = new LoanCalculatorMock();
        RiskAssessmentService riskService = new RiskAssessmentMock();
        loanService = new LoanApplicationComponent(checkService, riskService, calculatorService);
    }

    /**
     * Mock for the credit check service
     */
    private class CreditCheckServiceMock implements CreditCheckService {
        public int checkCredit(String ssn) {
            return 700;
        }
    }

    private class LoanCalculatorMock implements LoanCalculator {

        public LoanResult calculateOptions(LoanApplication application) {
            LoanResult result = new LoanResult();
            result.setResult(LoanResult.Approved);
            return result;
        }
    }

    private class RiskAssessmentMock implements RiskAssessmentService {

        public int assessRisk(LoanApplication loanApp) {
            return 0;
        }
    }
}
