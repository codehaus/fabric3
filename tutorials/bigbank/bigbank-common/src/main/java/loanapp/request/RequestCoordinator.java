/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package loanapp.request;

import loanapp.loan.LoanException;
import loanapp.message.LoanRequest;
import org.osoa.sca.annotations.Conversational;

/**
 * Coordinator that proccesses new loan requests.
 *
 * @version $Revision$ $Date$
 */
@Conversational
public interface RequestCoordinator {

    /**
     * Apply for a loan
     *
     * @param request the loan application data
     * @return the loan request id
     * @throws LoanException thrown if an error with the loan application is found
     */
    long start(LoanRequest request) throws LoanException;

    /**
     * Returns the status for a loan application
     *
     * @param id the loan id
     * @return the status
     */
    int getStatus(long id);

    /**
     * Cancel an in-process loan application
     */
    void cancel();

}
