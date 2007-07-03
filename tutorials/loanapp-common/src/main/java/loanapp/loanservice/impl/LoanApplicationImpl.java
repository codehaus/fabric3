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

import loanapp.loanservice.LoanApplication;

/**
 * @version $Rev$ $Date$
 */
public class LoanApplicationImpl implements LoanApplication {
    private String customerID;
    private double amount;
    private double downPayment;

    public LoanApplicationImpl(String customerID, double amount, double downPayment) {
        this.customerID = customerID;
        this.amount = amount;
        this.downPayment = downPayment;
    }

    public String getCustomerID() {
        return customerID;
    }

    public double getAmount() {
        return amount;
    }

    public double getDownPayment() {
        return downPayment;
    }
}
