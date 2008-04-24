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

import loanapp.credit.CreditScore;
import loanapp.credit.CreditService;
import loanapp.credit.CreditServiceCallback;
import loanapp.loan.LoanException;
import loanapp.message.LoanApplication;
import loanapp.message.LoanRequest;
import loanapp.message.LoanTerms;
import loanapp.pricing.PricingService;
import loanapp.risk.RiskAssessment;
import loanapp.risk.RiskAssessmentCallback;
import loanapp.risk.RiskAssessmentService;
import loanapp.store.StoreException;
import loanapp.store.StoreService;
import loanapp.notification.NotificationService;
import loanapp.acceptance.AcceptanceCoordinator;
import org.fabric3.api.annotation.Monitor;
import org.osoa.sca.annotations.ConversationAttributes;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Scope;
import org.osoa.sca.annotations.Service;

import java.util.UUID;

/**
 * Default implementation of the RequestCoordinator service.
 *
 * @version $Revision$ $Date$
 */
@Scope("CONVERSATION")
@ConversationAttributes(maxIdleTime = "2 hours")
@Service(interfaces = {RequestCoordinator.class, CreditServiceCallback.class, RiskAssessmentCallback.class})
public class RequestCoordinatorImpl implements RequestCoordinator, CreditServiceCallback, RiskAssessmentCallback {
    private CreditService creditService;
    private RiskAssessmentService riskService;
    private PricingService pricingService;
    private NotificationService notificationService;
    private StoreService storeService;
    private RequestCoordinatorMonitor monitor;
    private LoanApplication application;
    private AcceptanceCoordinator acceptanceCoordinator;

    /**
     * Creates a new instance.
     *
     * @param creditService       returns the applicant's credit score from a credit bureau
     * @param riskService         scores the loan risk
     * @param pricingService      calculates loan options
     * @param notificationService notifies the loan applicant of loan events
     * @param storeService        stores an application after it has been processed
     * @param monitor             the monitor for recording errors
     */
    public RequestCoordinatorImpl(@Reference(name = "creditService")CreditService creditService,
                                  @Reference(name = "riskService")RiskAssessmentService riskService,
                                  @Reference(name = "pricingService")PricingService pricingService,
                                  @Reference(name = "notificationService")NotificationService notificationService,
                                  @Reference(name = "storeService")StoreService storeService,
                                  @Monitor RequestCoordinatorMonitor monitor) {
        this.creditService = creditService;
        this.riskService = riskService;
        this.pricingService = pricingService;
        this.notificationService = notificationService;
        this.storeService = storeService;
        this.monitor = monitor;
    }

    public String start(LoanRequest request) throws LoanException {
        // create a loan application and process it
        application = new LoanApplication();
        application.setSSN(request.getSSN());
        application.setEmail(request.getEmail());
        application.setAmount(request.getAmount());
        application.setDownPayment(request.getDownPayment());
        application.setPropertyLocation(request.getPropertyLocation());
        String id = UUID.randomUUID().toString();
        application.setId(id);
        // pull the applicant's credit score
        creditService.score(application.getSSN());
        return id;
    }

    public void cancel() {
        throw new UnsupportedOperationException();
    }

    public void onCreditScore(CreditScore result) {
        // assess the loan risk
        application.setCreditScore(result);
        riskService.assessRisk(application);
    }

    public void creditScoreError(Exception exception) {
        monitor.error(exception);
    }

    public void onAssessment(RiskAssessment assessment) {
        application.setRiskAssessment(assessment);
        if (RiskAssessment.APPROVED == assessment.getDecision()) {
            // calculate the terms
            LoanTerms terms = pricingService.calculateOptions(application);
            application.setTerms(terms);
        }
        try {
            storeService.save(application);
            // notify the client
            notificationService.termsReady(application.getEmail(), application.getId());
        } catch (StoreException e) {
            monitor.error(e);
        }

        // xcv REMOVE
        try {
            acceptanceCoordinator.accept(application.getId());
        } catch (LoanException e) {
            throw new RuntimeException(e);
        }
    }

    public void riskAssessmentError(Exception exception) {
        monitor.error(exception);
    }

    // XCV
    @Reference
    public void setAcceptanceCoordinator(AcceptanceCoordinator acceptanceCoordinator) {
        this.acceptanceCoordinator = acceptanceCoordinator;
    }
}
