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
package org.fabric3.samples.bigbank.credit.impl;

import org.fabric3.samples.bigbank.credit.CreditBureau;

/**
 * Simulates a third-party credit service. In a real production system, this would be replaced by a bound reference pointing to an external
 * third-party service on the credit service.
 *
 * @version $Rev$ $Date$
 */
public class BDCreditBureau implements CreditBureau {

    public int score(String ein) {
        if (ein.endsWith("123")) {
            return 500;
        } else {
            return 750;
        }
    }
}