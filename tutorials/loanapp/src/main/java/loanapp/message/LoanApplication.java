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

import loanapp.risk.RiskAssessment;
import loanapp.credit.CreditScore;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulates loan application data. Contained data is built up as the application progresses through the loan
 * process.
 *
 * @version $Rev$ $Date$
 */
@XmlRootElement
public class LoanApplication implements Serializable {
    private static final long serialVersionUID = -1205831596861744741L;
    private String id;
    private long expiration;

    private String ssn;
    private double amount;
    private double downPayment;
    private PropertyLocation propertyLocation;
    private RiskAssessment riskAssessment;
    private LoanTerms terms;
    private CreditScore creditScore;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the applicant's Social Security Number
     *
     * @param ssn the applicant's SSN;
     */
    public void setSSN(String ssn) {
        this.ssn = ssn;
    }

    /**
     * Returns the applicant's Social Security Number
     *
     * @return the applicant's SSN;
     */
    public String getSSN() {
        return ssn;
    }

    public PropertyLocation getPropertyLocation() {
        return propertyLocation;
    }

    public void setPropertyLocation(PropertyLocation location) {
        this.propertyLocation = location;
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

    /**
     * Returns the applicant's credit score
     *
     * @return the applicant's credit score
     */
    public CreditScore getCreditScore() {
        return creditScore;
    }

    /**
     * Sets the applicant's credit score
     *
     * @param creditScore the applicant's credit score
     */
    public void setCreditScore(CreditScore creditScore) {
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

    public void setTerms(LoanTerms terms) {
        this.terms = terms;
    }

    public LoanTerms getTerms() {
        return terms;
    }
}
