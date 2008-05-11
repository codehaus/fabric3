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
package loanapp.message;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Encapsulates loan application data. Contained data is built up as the application progresses through the loan
 * process.
 *
 * @version $Rev$ $Date$
 */
@XmlRootElement
public class LoanApplication implements Serializable {
    private static final long serialVersionUID = -1205831596861744741L;
    private long number;
    private long expiration;
    private int status;
    private String ssn;
    private String email;
    private double amount;
    private double downPayment;
    private Address propertyAddress;
    private RiskAssessment riskAssessment;
    private Term[] terms;
    private int creditScore;

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    /**
     * Returns the loan status as defined in {@link loanapp.message.LoanStatus}.
     *
     * @return the loan status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the loan status as defined in {@link loanapp.message.LoanStatus}.
     *
     * @param status the loan status
     */
    public void setStatus(int status) {
        this.status = status;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the loan amount.
     *
     * @return the loan amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the loan amount.
     *
     * @param amount the loan amount
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Returns the loan downpayment amount.
     *
     * @return the loan downpayment amount
     */
    public double getDownPayment() {
        return downPayment;
    }

    /**
     * Sets the loan downpayment amount.
     *
     * @param downPayment loan downpayment amount
     */
    public void setDownPayment(double downPayment) {
        this.downPayment = downPayment;
    }

    public Address getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(Address address) {
        this.propertyAddress = address;
    }

    /**
     * Returns the applicant's credit score
     *
     * @return the applicant's credit score
     */
    public int getCreditScore() {
        return creditScore;
    }

    /**
     * Sets the applicant's credit score
     *
     * @param creditScore the applicant's credit score
     */
    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    /**
     * Returns the applicant's risk assesment.
     *
     * @return the applicant's risk assesment
     */
    public RiskAssessment getRiskAssessment() {
        return riskAssessment;
    }

    /**
     * Sets the applicant's risk assesment.
     *
     * @param assessment the applicant's risk assesment
     */
    public void setRiskAssessment(RiskAssessment assessment) {
        this.riskAssessment = assessment;
    }


    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public void setTerms(Term[] terms) {
        this.terms = terms;
    }

    public Term[] getTerms() {
        return terms;
    }

}

