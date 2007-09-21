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
package loanapp.calculator;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Scope;

import loanapp.message.LoanApplication;
import loanapp.message.LoanOption;
import loanapp.message.LoanResult;
import loanapp.rate.Rate;
import loanapp.rate.RateResults;
import loanapp.rate.RateService;

/**
 * Default implementation of the LoanCalculator that uses a RateService to compile up-to-date loan options.
 *
 * @version $Rev$ $Date$
 */
//@Scope("COMPOSITE")
public class LoanCalculatorComponent implements LoanCalculator {
    private RateService rateService;

    public LoanCalculatorComponent(@Reference(name = "rateService")RateService rateService) {
        this.rateService = rateService;
    }

    public LoanResult calculateOptions(LoanApplication application) {
        LoanResult result = new LoanResult();
        if (application.getResult() == LoanResult.DECLINED) {
            result.setResult(LoanResult.DECLINED);
            result.addReasons(application.getRiskReasons());
            return result;
        }
        result.setResult(LoanResult.APPROVED);
        RateResults rateResults = rateService.getRates(application.getRisk());
        for (Rate rate : rateResults.getRates()) {
            LoanOption option = new LoanOption(rate.getType(), rate.getRate(), rate.getApr());
            result.addOption(option);
        }
        return result;
    }
}
