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
package loanapp;

import loanapp.loanservice.LoanApplication;
import loanapp.loanservice.LoanApplicationService;
import loanapp.loanservice.LoanResult;
import loanapp.loanservice.impl.LoanApplicationImpl;
import org.fabric3.runtime.development.Domain;

/**
 * Main class demonstrating how to setup a single domain in an IDE, activate the loan app composite, and connect to the
 * LoanApplicationService.
 *
 * @version $Rev$ $Date$
 */
public class LoanApp {
    private Domain domain;

    /**
     * Main entry point
     *
     * @param args an array container the customer id, the loan amount, and the downpayment amount
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("ID, amount, and downpayment required");
        }
        LoanApp app = new LoanApp();
        app.setup();
        app.run(args[0], Double.valueOf(args[1]), Double.valueOf(args[2]));
        System.exit(0);
    }

    /**
     * Initializes the local domain and activates the loan app composite
     */
    public void setup() {
        domain = new Domain();
        domain.activate(Thread.currentThread().getContextClassLoader().getResource("META-INF/loanapp.composite"));
    }

    /**
     * Makes a loan application request
     *
     * @param id     the customer id
     * @param amount the amount of the lown
     * @param down   the downpayment amount
     */
    public void run(String id, double amount, double down) {
        LoanApplicationService loanService = domain.connectTo(LoanApplicationService.class, "LoanApplicationComponent");
        LoanApplication application = new LoanApplicationImpl(id, amount, down);
        LoanResult result = loanService.applyForLoan(application);
        if (result.getCode() == LoanResult.APPROVED) {
            System.out.println("Congratulations, your loan was approved");
        } else {
            System.out.println("Sorry, your loan was declined");
        }
    }
}
