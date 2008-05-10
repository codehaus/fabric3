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
package loanapp.pricing;

import loanapp.message.LoanApplication;
import loanapp.message.Term;
import loanapp.rate.Rate;
import loanapp.rate.RateResults;
import loanapp.rate.RateService;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the PricingService that uses a RateService to compile up-to-date loan options.
 *
 * @version $Rev$ $Date$
 */
@Scope("COMPOSITE")
public class RiskBasedPricingComponent implements PricingService {
    private RateService rateService;

    public RiskBasedPricingComponent(@Reference(name = "rateService")RateService rateService) {
        this.rateService = rateService;
    }

    public Term[] calculateOptions(LoanApplication application) {
        List<Term> terms = new ArrayList<Term>();
        RateResults rateResults = rateService.calculateRates(application.getRiskAssessment().getRiskFactor());
        for (Rate rate : rateResults.getRates()) {
            terms.add(new Term(rate.getType(), rate.getRate(), rate.getApr()));
        }
        return terms.toArray(new Term[terms.size()]);
    }
}
