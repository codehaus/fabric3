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
package loanapp.loanservice.impl;

import loanapp.loanservice.LoanResult;

/**
 * @version $Rev$ $Date$
 */
public class LoanResultImpl implements LoanResult {
    private int code;
    private double rate;

    public LoanResultImpl(int result, double rate) {
        this.code = result;
        this.rate = rate;
    }

    public int getCode() {
        return code;
    }

    public double getRate() {
        return rate;
    }

    public LoanResultImpl(int result) {
        this.code = result;
    }
}
