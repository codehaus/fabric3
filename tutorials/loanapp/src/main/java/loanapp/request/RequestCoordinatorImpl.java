package loanapp.request;

import loanapp.credit.CreditScore;
import loanapp.credit.CreditService;
import loanapp.credit.CreditServiceCallback;
import loanapp.loan.LoanException;
import loanapp.message.LoanApplication;
import loanapp.message.LoanRequest;
import loanapp.message.LoanTerms;
import loanapp.pricing.PricingService;
import loanapp.risk.RiskAssessment;
import loanapp.risk.RiskAssessmentCallback;
import loanapp.risk.RiskAssessmentService;
import loanapp.store.StoreException;
import loanapp.store.StoreService;
import org.fabric3.api.annotation.Monitor;
import org.osoa.sca.annotations.ConversationAttributes;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Scope;
import org.osoa.sca.annotations.Service;

import java.util.UUID;

/**
 * Default implementation of the RequestCoordinator service.
 *
 * @version $Revision$ $Date$
 */
@Scope("CONVERSATION")
@ConversationAttributes(maxIdleTime = "2 hours")
@Service(interfaces = {RequestCoordinator.class, CreditServiceCallback.class, RiskAssessmentCallback.class})
public class RequestCoordinatorImpl implements RequestCoordinator, CreditServiceCallback, RiskAssessmentCallback {
    private CreditService creditService;
    private RiskAssessmentService riskService;
    private PricingService pricingService;
    private StoreService storeService;
    private RequestCoordinatorMonitor monitor;
    private LoanApplication application;

    /**
     * Creates a new instance.
     *
     * @param creditService  returns the applicant's credit score from a credit bureau
     * @param riskService    scores the loan risk
     * @param pricingService calculates loan options
     * @param storeService   stores an application after it has been processed
     * @param monitor        the monitor for recording errors
     */
    public RequestCoordinatorImpl(@Reference(name = "creditService")CreditService creditService,
                                  @Reference(name = "riskService")RiskAssessmentService riskService,
                                  @Reference(name = "pricingService")PricingService pricingService,
                                  @Reference(name = "storeService")StoreService storeService,
                                  @Monitor RequestCoordinatorMonitor monitor) {
        this.creditService = creditService;
        this.riskService = riskService;
        this.pricingService = pricingService;
        this.storeService = storeService;
        this.monitor = monitor;
    }

    public String start(LoanRequest request) throws LoanException {
        // create a loan application and process it
        application = new LoanApplication();
        application.setAmount(request.getAmount());
        application.setDownPayment(request.getDownPayment());
        application.setSSN(request.getSSN());
        application.setPropertyLocation(request.getPropertyLocation());
        String id = UUID.randomUUID().toString();
        application.setId(id);
        // pull the applicant's credit score
        creditService.score(application.getSSN());
        return id;
    }

    public void cancel() {

    }

    public void onCreditScore(CreditScore result) {
        // assess the loan risk
        application.setCreditScore(result);
        riskService.assessRisk(application);
    }

    public void creditScoreError(Exception exception) {
        // TODO something better
        monitor.error(exception);
    }


    public void onAssessment(RiskAssessment assessment) {
        application.setRiskAssessment(assessment);
        if (RiskAssessment.APPROVED == assessment.getDecision()) {
            // calculate the terms
            LoanTerms terms = pricingService.calculateOptions(application);
            application.setTerms(terms);
        }
        try {
            storeService.save(application);
            // TODO send notification to client
        } catch (StoreException e) {
            // TODO something better
            monitor.error(e);
        }
    }

    public void riskAssessmentError(Exception exception) {
        // TODO something better
        monitor.error(exception);
    }
}
