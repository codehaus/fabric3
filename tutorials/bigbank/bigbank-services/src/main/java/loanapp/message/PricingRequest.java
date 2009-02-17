package loanapp.message;

/**
 * @version $Revision$ $Date$
 */
public class PricingRequest {
    private int riskFactor;

    public PricingRequest(int riskFactor) {
        this.riskFactor = riskFactor;
    }

    public int getRiskFactor() {
        return riskFactor;
    }
}
