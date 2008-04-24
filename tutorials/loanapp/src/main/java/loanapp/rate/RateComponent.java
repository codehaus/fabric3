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
package loanapp.rate;

import org.osoa.sca.annotations.Scope;

/**
 * @version $Rev: 1366 $ $Date: 2007-09-20 21:46:05 -0700 (Thu, 20 Sep 2007) $
 */
@Scope("COMPOSITE")
public class RateComponent implements RateService {

    public RateResults calculateRates(int risk) {
        RateResults results = new RateResults();
        if (risk == 1) {
            Rate fixed30 = new Rate("30 Year FIXED", 5.5f, 1f);
            Rate arm30 = new Rate("30 Year ARM", 5.0f, 0f);
            results.addRate(fixed30);
            results.addRate(arm30);
        } else if (risk > 1 && risk < 5) {
            Rate fixed30 = new Rate("30 Year FIXED", 5.3f, 2f);
            Rate arm30 = new Rate("30 Year ARM", 5.1f, 1f);
            results.addRate(fixed30);
            results.addRate(arm30);
        } else if (risk == 5) {
            Rate fixed30 = new Rate("30 Year FIXED", 5.5f, 3f);
            Rate arm30 = new Rate("30 Year ARM", 5.2f, 2f);
            results.addRate(fixed30);
            results.addRate(arm30);
        } else if (risk > 5 && risk < 10) {
            Rate fixed30 = new Rate("30 Year FIXED", 6.0f, 4f);
            results.addRate(fixed30);
        }
        return results;
    }
}
