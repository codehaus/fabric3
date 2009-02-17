package loanapp.message;

/**
 * @version $Revision$ $Date$
 */
public class RiskRequest {
    private long id;
    private int creditScore;
    private double downPayment;
    private double amount;

    public RiskRequest(long id, int creditScore, double amount, double downPayment) {
        this.id = id;
        this.creditScore = creditScore;
        this.amount = amount;
        this.downPayment = downPayment;
    }

    public long getId() {
        return id;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public double getAmount() {
        return amount;
    }

    public double getDownPayment() {
        return downPayment;
    }
}
