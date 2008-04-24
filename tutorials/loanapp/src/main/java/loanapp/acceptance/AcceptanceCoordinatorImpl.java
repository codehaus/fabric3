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
package loanapp.acceptance;

import loanapp.store.StoreService;
import loanapp.store.StoreException;
import loanapp.message.LoanApplication;
import loanapp.loan.LoanException;
import loanapp.appraisal.AppraisalService;
import loanapp.appraisal.AppraisalCallback;
import loanapp.appraisal.AppraisalResult;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Scope;
import org.osoa.sca.annotations.Service;

/**
 * Default implementation of the AcceptanceCoordinator.
 *
 * @version $Revision$ $Date$
 */
@Scope("CONVERSATION")
@Service(interfaces = {AcceptanceCoordinator.class, AppraisalCallback.class})
public class AcceptanceCoordinatorImpl implements AcceptanceCoordinator, AppraisalCallback {
    private AppraisalService appraisalService;
    private StoreService storeService;
    private LoanApplication application;

    public AcceptanceCoordinatorImpl(@Reference(name = "appraisalService")AppraisalService appraisalService,
                                     @Reference(name = "storeService")StoreService storeService) {
        this.appraisalService = appraisalService;
        this.storeService = storeService;
    }


    public void accept(String id) throws LoanException {
        try {
            application = storeService.find(id);
        } catch (StoreException e) {
            throw new LoanException(e);
        }
        if (application.getExpiration() <= System.currentTimeMillis()) {
            throw new LoanOfferExpiredException("Loan expired: " + id);
        }
        // lock loan
        // send to appraisal service
        // get callback from appraisal
        // send to closing system
        // get callback 

    }

    public void decline(String loanId) throws LoanException {

    }

    public void dateSchedule(long time) {

    }

    public void appraisalCompleted(AppraisalResult result) {

    }
}
