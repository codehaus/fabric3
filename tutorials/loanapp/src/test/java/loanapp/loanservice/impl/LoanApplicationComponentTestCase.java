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
import loanapp.credit.CreditCheckService;
import loanapp.loanservice.LoanApplicationService;

/**
 * Demonstrates unit testing outside the Fabric3 container. For automated mock object creation and verification,
 * EasyMock (www.easymock.org) is recommended.
 *
 * @version $Rev$ $Date$
 */
public class LoanApplicationComponentTestCase extends TestCase {
    private LoanApplicationComponent component;

    /**
     * Verifies the loan application component can receive and application and approve it.
     */
    public void testLoanApplication() {
        assertTrue(LoanApplicationService.DECLINED != component.applyForLoan("123", 1000, 100));
    }

    /**
     * Test fixture
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        super.setUp();
        component = new LoanApplicationComponent(new CreditCheckServiceMock());
    }

    /**
     * Mock for the credit check service
     */
    private class CreditCheckServiceMock implements CreditCheckService {
        public int checkCredit(String id) {
            return 700;
        }
    }
}
