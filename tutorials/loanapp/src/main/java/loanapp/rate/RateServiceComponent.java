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

/**
 * @version $Rev$ $Date$
 */
public class RateServiceComponent implements RateService {

    public RateResults getRates(int risk) {
        Rate fixed30 = new Rate("30 Year FIXED", 5.9f, 2f);
        Rate arm30 = new Rate("30 Year ARM", 5.0f, 1f);
        RateResults results = new RateResults();
        results.addRate(fixed30);
        results.addRate(arm30);
        return results;
    }
}
