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

import loanapp.message.Loan;
import loanapp.message.PricingResponse;
import loanapp.message.PricingRequest;
import org.oasisopen.sca.annotation.Remotable;

/**
 * Implementations compile a set of different loan options that fit the characteristics of an applicant and loan
 * amount.
 *
 * @version $Rev$ $Date$
 */
@Remotable
public interface PricingService {

    /**
     * Compiles the set of loan options for an application
     *
     * @return a set of loan options
     */
    PricingResponse[] calculateOptions(PricingRequest request);
}
