package loanapp.loanservice;

/**
 * @version $Rev$ $Date$
 */
public interface LoanResult {
    int APPROVED = 1;
    int DECLINED = -1;

    int getCode();

    double getRate();

}
