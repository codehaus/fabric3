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

import loanapp.message.LoanApplication;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Scope;
import org.osoa.sca.annotations.Callback;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation that performs risk assesment based on an applicant's credit score and loan amount.
 *
 * @version $Rev$ $Date$
 */
@Scope("COMPOSITE")
public class RiskAssessmentComponent implements RiskAssessmentService {
    private RiskAssessmentCallback callback;
    private double ratioMinimum;

    public RiskAssessmentComponent(@Property(name = "ratioMinimum", required = true)Double ratioMinimum) {
        this.ratioMinimum = ratioMinimum;
    }

    @Callback
    public void setCallback(RiskAssessmentCallback callback) {
        this.callback = callback;
    }

    public void assessRisk(LoanApplication application) {
        int score = application.getCreditScore();
        int factor = 0;
        List<String> reasons = new ArrayList<String>();
        if (score < 700) {
            factor += 10;
            reasons.add("Poor credit history");
        }
        double ratio = application.getDownPayment() / application.getAmount();
        if (ratio < ratioMinimum) {
            // less than a minimum percentage down, so assign it the highest risk
            factor += 15;
            reasons.add("Downpayment was too little");
            reasons.add("Suspect credit history");
        }
        RiskAssessmentResult result = new RiskAssessmentResult(factor, reasons);
        callback.onAssessment(result);
    }
}
