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
package loanapp.api.loan;

import loanapp.api.message.LoanRequest;
import loanapp.api.message.LoanData;
import loanapp.api.loan.LoanNotFoundException;
import org.oasisopen.sca.annotation.Remotable;

/**
 * Implementations process a loan application.
 *
 * @version $Rev$ $Date$
 */
@Remotable
public interface LoanService {

    /**
     * Interface to the loan application process.
     *
     * @param request the loan request data
     * @return the loan tracking number
     * @throws LoanException
     */
    long apply(LoanRequest request) throws LoanException;

    LoanData review(long id) throws LoanNotFoundException;

    void decline(long id) throws LoanException;

    void accept(OptionSelection selection) throws LoanException;

}
