package loanapp;

import loanapp.loanservice.LoanApplicationService;
import loanapp.message.LoanRequest;
import loanapp.message.LoanResult;
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
            throw new IllegalArgumentException("SSN, amount, and downpayment required");
        }
        LoanApp app = new LoanApp();
        app.setup();
        app.run(args[0], Double.valueOf(args[1]), Double.valueOf(args[2]));
        System.exit(0);
    }

    /**
     * Initializes the local domain and activates the loan app composite
     */
    private void setup() {
        domain = new Domain();
        domain.activate(Thread.currentThread().getContextClassLoader().getResource("META-INF/loanappClient.composite"));
    }

    /**
     * Makes a loan application request
     *
     * @param ssn         the customer id
     * @param amount      the amount of the lown
     * @param downPayment the downpayment amount
     */
    private void run(String ssn, double amount, double downPayment) {
        LoanApplicationService loanService = domain.connectTo(LoanApplicationService.class, "LoanAppClient");
        LoanRequest request = new LoanRequest();
        request.setSSN(ssn);
        request.setAmount(amount);
        request.setDownPayment(downPayment);
        LoanResult result = loanService.apply(request);
        if (result.getResult() == LoanResult.DECLINED) {
            System.out.println("Sorry, your loan was declined");
        } else {
            System.out.println("Congratulations, your loan was approved");
        }
        domain.stop();
    }
}
