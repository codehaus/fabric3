package loanapp.bigbrother;

import loanapp.credit.CreditScore;
import org.oasisopen.sca.annotation.Scope;

/**
 * Audits credit scoring operations for compliance reasons.
 *
 * @version $Revision$ $Date$
 */
@Scope("COMPOSITE")
public class AuditServiceImpl implements AuditService {

    public void recordCheck(String ssn) {
        System.out.println("AuditService: Credit check for " + ssn);
    }

    public void recordResult(String ssn, CreditScore score) {
        System.out.println("AuditService: Credit result received for " + ssn + ". Score was "
                + score.getScore() + ".");
    }
}
