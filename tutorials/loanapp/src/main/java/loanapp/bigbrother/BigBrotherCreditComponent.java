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
package loanapp.bigbrother;

import loanapp.credit.CreditScore;
import loanapp.credit.CreditService;
import loanapp.credit.CreditServiceCallback;
import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Reference;

/**
 * Implementation of a CreditService that returns a credit score from the fictitious BigBrother credit bureau.
 * This implementation records all credit score operations with the AuditService.
 *
 * @version $Rev: 1366 $ $Date: 2007-09-20 21:46:05 -0700 (Thu, 20 Sep 2007) $
 */
public class BigBrotherCreditComponent implements CreditService {
    private AuditService auditService;
    private CreditServiceCallback callbackService;

    public BigBrotherCreditComponent(@Reference(name = "auditService")AuditService auditService) {
        this.auditService = auditService;
    }

    @Callback
    public void setCallbackService(CreditServiceCallback callbackService) {
        this.callbackService = callbackService;
    }

    public void score(String ssn) {
        auditService.recordCheck(ssn);
        CreditScore score;
        if ("111111111".equals(ssn)) {
            score = new CreditScore(300, new int[0]);
        } else if ("222222222".equals(ssn)) {
            score = new CreditScore(700, new int[0]);
        } else {
            score = new CreditScore(760, new int[0]);
        }
        auditService.recordResult(ssn, score);
        callbackService.onCreditScore(score);
    }
}