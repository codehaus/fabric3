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

import org.osoa.sca.annotations.Remotable;

import loanapp.message.LoanRequest;
import loanapp.message.LoanResult;

/**
 * Implementations process a loan application.
 *
 * @version $Rev$ $Date$
 */
@Remotable
public interface LoanApplicationService {

    /**
     * Initiates the loan application process.
     *
     * @param request the loan request data
     * @return a result that indicates if the application was accepted or declined. In the case the application is
     *         accepted, the result message will contain the available options an applicant may select from.
     */
    LoanResult apply(LoanRequest request);

}
