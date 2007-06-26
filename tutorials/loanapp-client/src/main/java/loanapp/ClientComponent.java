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

import org.osoa.sca.annotations.Reference;

import loanapp.loanservice.LoanApplication;
import loanapp.loanservice.LoanApplicationService;
import loanapp.loanservice.LoanResult;
import loanapp.loanservice.impl.LoanApplicationImpl;

/**
 * @version $Rev$ $Date$
 */
public class ClientComponent {
    private LoanApplicationService loanService;

    public ClientComponent(@Reference(name = "loanService")LoanApplicationService loanService) {
        this.loanService = loanService;
    }

    public int main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("ID, amount, and downpayment required");
        }
        LoanApplication app = new LoanApplicationImpl(args[0], Double.valueOf(args[1]), Double.valueOf(args[2]));
        LoanResult result = loanService.applyForLoan(app);
        if (result.getCode() == LoanResult.APPROVED) {
            System.out.println("Congratulations, your loan was approved");
        } else {
            System.out.println("Sorry, your loan was declined");
        }
        return 0;
    }

}
