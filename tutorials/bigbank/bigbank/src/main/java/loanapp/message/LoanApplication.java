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

import loanapp.credit.CreditScore;
import loanapp.risk.RiskAssessment;
import loanapp.rate.Rate;

import javax.xml.bind.annotation.XmlRootElement;
import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Encapsulates loan application data. Contained data is built up as the application progresses through the loan
 * process.
 *
 * @version $Rev$ $Date$
 */
@XmlRootElement
@Entity
public class LoanApplication implements Serializable {
    private static final long serialVersionUID = -1205831596861744741L;
    private long id;
    private long version;
    private long expiration;
    private int status;
    private String ssn;
    private String email;
    private double amount;
    private double downPayment;
    private String typeSelected;
    private PropertyInfo propertyInfo;
    private RiskAssessment riskAssessment;
    private List<Term> terms;
    private CreditScore creditScore;


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Version
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
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

    @OneToOne(cascade = CascadeType.ALL)
    public PropertyInfo getPropertyInfo() {
        return propertyInfo;
    }

    public void setPropertyInfo(PropertyInfo propertyInfo) {
        this.propertyInfo = propertyInfo;
    }

    /**
     * Returns the applicant's credit score
     *
     * @return the applicant's credit score
     */
    @OneToOne(cascade = CascadeType.ALL)
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
    @OneToOne(cascade = CascadeType.ALL)
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

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<Term> getTerms() {
        return terms;
    }

    public String getTypeSelected() {
        return typeSelected;
    }

    public void setTypeSelected(String typeSelected) {
        this.typeSelected = typeSelected;
    }

    @Transient
    public Term getSelectedOption() {
        if (typeSelected == null) {
            return null;
        }
        for (Term term : terms) {
            if (term.getType().equals(typeSelected)) {
                return term;
            }
        }
        return null;
    }


}

