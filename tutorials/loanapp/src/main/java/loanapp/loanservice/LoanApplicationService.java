package loanapp.loanservice;

import org.osoa.sca.annotations.Remotable;

/**
 * Implementations process a loan application.
 *
 * @version $Rev$ $Date$
 */
@Remotable
public interface LoanApplicationService {

    int DECLINED = -1;

    double applyForLoan(String id, double amount, double downpayment);
}
