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

import loanapp.loanservice.LoanApplicationService;
import org.fabric3.runtime.development.Domain;

/**
 * Demonstrates running the loan service application in an IDE environment.
 *
 * @version $Rev$ $Date$
 */
public class LoanApp {

    public static void main(String[] args) {
        Domain domain = new Domain();
        domain.activate(Thread.currentThread().getContextClassLoader().getResource("META-INF/loanapp.composite"));
        LoanApplicationService loanService = domain.connectTo(LoanApplicationService.class, "LoanApplicationComponent");
        double result = loanService.applyForLoan("100", 100000, 10000);
        if (result == LoanApplicationService.DECLINED) {
            System.out.println("Sorry, your loan was declined");
        } else {
            System.out.println("Congratulations, your loan was approved");
        }
        domain.stop();
    }
}
