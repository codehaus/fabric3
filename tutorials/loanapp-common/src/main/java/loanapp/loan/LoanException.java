package loanapp.loan;

/**
 * @version $Revision$ $Date$
 */
public class LoanException extends Exception {
    private static final long serialVersionUID = 7596067031743965923L;

    public LoanException(String message) {
        super(message);
    }

    public LoanException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoanException(Throwable cause) {
        super(cause);
    }
}
