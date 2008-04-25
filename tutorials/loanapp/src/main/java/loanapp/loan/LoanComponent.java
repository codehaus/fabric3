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
package loanapp.loan;

import loanapp.acceptance.AcceptanceCoordinator;
import loanapp.message.LoanRequest;
import loanapp.request.RequestCoordinator;
import loanapp.validation.ValidationResult;
import loanapp.validation.ValidationService;
import org.osoa.sca.annotations.Reference;

/**
 * Default implementation of the LoanService. This implementation performs basic validation and delegates to a series
 * of coordinators that process the loan application.
 *
 * @version $Rev$ $Date$
 */
public class LoanComponent implements LoanService {
    private ValidationService validationService;
    private RequestCoordinator requestCoordinator;
    private AcceptanceCoordinator acceptanceCoordinator;

    /**
     * Creates a new instance.
     *
     * @param validationService     the service to  validate incoming requests
     * @param requestCoordinator    coordinator that handles new request processing
     * @param acceptanceCoordinator coordinator that handles acceptance and rejection processing
     */
    public LoanComponent(@Reference(name = "validationService")ValidationService validationService,
                         @Reference(name = "requestCoordinator")RequestCoordinator requestCoordinator,
                         @Reference(name = "acceptanceCoordinator")AcceptanceCoordinator acceptanceCoordinator) {
        this.validationService = validationService;
        this.requestCoordinator = requestCoordinator;
        this.acceptanceCoordinator = acceptanceCoordinator;
    }

    public String apply(LoanRequest request) throws LoanException {
        // validate the loan request
        ValidationResult result = validationService.validateRequest(request);
        if (!result.isValid()) {
            // todo report errors
            throw new InvalidLoanData("Data was invalid");
        }
        return requestCoordinator.start(request);
    }

    public void decline(String id) throws LoanException {
        acceptanceCoordinator.review(id);
        acceptanceCoordinator.decline();
    }

    public void accept(String id) throws LoanException {
        acceptanceCoordinator.review(id);
        acceptanceCoordinator.accept();
    }

}
