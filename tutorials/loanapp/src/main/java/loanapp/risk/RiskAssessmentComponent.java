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
package loanapp.risk;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Scope;

import loanapp.message.LoanApplication;
import loanapp.message.LoanResult;

/**
 * @version $Rev$ $Date$
 */
//@Scope("COMPOSITE")
public class RiskAssessmentComponent implements RiskAssessmentService {
    private double ratioMinimum;


    public RiskAssessmentComponent(@Property(name = "ratioMinimum", required = true)Double ratioMinimum) {
        this.ratioMinimum = ratioMinimum;
    }

    public LoanApplication assessRisk(LoanApplication application) {
        int score = application.getCreditScore();
        if (score < 700) {
            application.addRiskReason("Poor credit history");
            application.setRisk(10);
            application.setResult(LoanResult.DECLINED);
            return application;
        }
        double ratio = application.getDownPayment() / application.getAmount();
        if (ratio < ratioMinimum) {
            // less than a minimum percentage down, so assign it the highest risk
            application.addRiskReason("Downpayment was to little");
            application.addRiskReason("Suspect credit history");
            application.setRisk(10);
            application.setResult(LoanResult.DECLINED);
        }
        if (score > 750) {
            application.setRisk(1);
            application.setResult(LoanResult.APPROVED);
        } else {
            application.setRisk(5);
            application.setResult(LoanResult.APPROVED);
        }
        return application;
    }
}
