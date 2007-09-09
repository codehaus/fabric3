package loanapp;

import loanapp.loanservice.LoanApplicationService;
import org.fabric3.runtime.development.Domain;

/**
 * Main class demonstrating how to setup a single domain in an IDE, activate the loan app composite, and connect to the
 * LoanApplicationService.
 *
 * @version $Rev$ $Date$
 */
public class LoanApp {
    private Domain domain;

    /**
     * Main entry point
     *
     * @param args an array container the customer id, the loan amount, and the downpayment amount
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("ID, amount, and downpayment required");
        }
        LoanApp app = new LoanApp();
        app.setup();
        app.run(args[0], Double.valueOf(args[1]), Double.valueOf(args[2]));
        System.exit(0);
    }

    /**
     * Initializes the local domain and activates the loan app composite
     */
    public void setup() {
        domain = new Domain();
        domain.activate(Thread.currentThread().getContextClassLoader().getResource("META-INF/loanappClient.composite"));
    }

    /**
     * Makes a loan application request
     *
     * @param id     the customer id
     * @param amount the amount of the lown
     * @param down   the downpayment amount
     */
    public void run(String id, double amount, double down) {
        LoanApplicationService loanService = domain.connectTo(LoanApplicationService.class, "LoanAppClient");
        double result = loanService.applyForLoan(id, amount, down);
        if (result == LoanApplicationService.DECLINED) {
            System.out.println("Sorry, your loan was declined");
        } else {
            System.out.println("Congratulations, your loan was approved");
        }
        domain.stop();
    }
}
