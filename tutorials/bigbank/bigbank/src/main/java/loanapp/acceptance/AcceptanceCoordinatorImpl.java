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

import loanapp.appraisal.AppraisalCallback;
import loanapp.appraisal.AppraisalResult;
import loanapp.appraisal.AppraisalService;
import loanapp.domain.LoanRecord;
import loanapp.domain.TermInfo;
import loanapp.loan.LoanException;
import loanapp.message.LoanOption;
import loanapp.message.LoanOptions;
import loanapp.message.LoanStatus;
import loanapp.notification.NotificationService;
import loanapp.store.StoreException;
import loanapp.store.StoreService;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Scope;
import org.osoa.sca.annotations.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Default implementation of the AcceptanceCoordinator.
 *
 * @version $Revision$ $Date$
 */
@Scope("CONVERSATION")
@Service(interfaces = {AcceptanceCoordinator.class, AppraisalCallback.class})
public class AcceptanceCoordinatorImpl implements AcceptanceCoordinator, AppraisalCallback {
    private AppraisalService appraisalService;
    private NotificationService notificationService;
    private StoreService storeService;
    private LoanRecord application;

    public AcceptanceCoordinatorImpl(@Reference(name = "appraisalService")AppraisalService appraisalService,
                                     @Reference(name = "notificationService")NotificationService notificationService,
                                     @Reference(name = "storeService")StoreService storeService) {
        this.appraisalService = appraisalService;
        this.notificationService = notificationService;
        this.storeService = storeService;
    }


    public LoanOptions review(long loanId) throws LoanException {
        findRecord(loanId);
        LoanOptions options = new LoanOptions();
        for (TermInfo term : application.getTerms()) {
            options.addOption(new LoanOption(term.getType(), term.getRate(), term.getApr()));
        }
        return options;
    }

    public void accept(String type) throws LoanException {
        List<TermInfo> terms = application.getTerms();
        boolean found = false;
        for (TermInfo term : terms) {
            if (term.getType().equals(type)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new InvalidLoanOptionException("Invalid loan option selected for loan " + application.getId());
        }
        application.setTypeSelected(type);
        application.setStatus(LoanStatus.AWAITING_APPRAISAL);
        try {
            storeService.update(application);
        } catch (StoreException e) {
            throw new LoanException(e);
        }
        // TODO lock loan
        appraisalService.appraise(application.getPropertyInfo().getAddress());
    }

    public void decline() throws LoanException {
        application.setStatus(LoanStatus.DECLINED);
        try {
            storeService.update(application);
        } catch (StoreException e) {
            throw new LoanException(e);
        }
    }

    public void dateSchedule(Date time) {
        notificationService.appraisalScheduled(application.getEmail(), application.getId(), time);
    }

    public void appraisalCompleted(AppraisalResult result) {
        // TODO add appraisal result
        if (AppraisalResult.DECLINED == result.getResult()) {
            // TODO notify
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        notificationService.fundingDateScheduled(application.getEmail(), application.getId(), calendar.getTime());
        // TODO send to closing system
    }

    private void findRecord(long id) throws LoanException {
        try {
            application = storeService.find(id);
        } catch (StoreException e) {
            throw new LoanException(e);
        }
        if (application == null) {
            throw new LoanNotFoundException("No loan application on file with id " + id);
        }
        if (LoanStatus.AWAITING_ACCEPTANCE != application.getStatus()) {
            throw new LoanNotApprovedException("Loan was not approved");
        }
    }


}
