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
package org.fabric3.samples.bigbank.loan.loan;

import  org.fabric3.samples.bigbank.loan.acceptance.AcceptanceCoordinator;
import org.fabric3.samples.bigbank.api.loan.LoanException;
import org.fabric3.samples.bigbank.api.loan.LoanService;
import org.fabric3.samples.bigbank.api.message.LoanApplication;
import org.fabric3.samples.bigbank.api.message.LoanRequest;
import org.fabric3.samples.bigbank.api.message.OptionSelection;
import org.fabric3.samples.bigbank.loan.request.RequestCoordinator;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default implementation of the LoanService. This implementation delegates to a series of coordinators that process the
 * loan application.
 *
 * @version $Rev: 8764 $ $Date: 2010-03-29 12:00:55 +0200 (Mon, 29 Mar 2010) $
 */
public class LoanComponent implements LoanService {
    private RequestCoordinator requestCoordinator;
    private AcceptanceCoordinator acceptanceCoordinator;

    /**
     * Creates a new instance.
     *
     * @param requestCoordinator    coordinator that handles new request processing
     * @param acceptanceCoordinator coordinator that handles acceptance and rejection processing
     */
    public LoanComponent(@Reference(name = "requestCoordinator") RequestCoordinator requestCoordinator,
                         @Reference(name = "acceptanceCoordinator") AcceptanceCoordinator acceptanceCoordinator) {
        this.requestCoordinator = requestCoordinator;
        this.acceptanceCoordinator = acceptanceCoordinator;
    }

    public long apply(LoanRequest request) throws LoanException {
        return requestCoordinator.start(request);
    }

    public LoanApplication retrieve(long id) throws LoanException {
        return acceptanceCoordinator.retrieve(id);
    }

    public void decline(long id) throws LoanException {
        acceptanceCoordinator.decline(id);
    }

    public void accept(OptionSelection selection) throws LoanException {
        acceptanceCoordinator.accept(selection.getId(), selection.getType());
    }

}
