package org.fabric3.tutorials.webcalc.calculator;

import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class CalculatorServiceImpl implements CalculatorService {
    private AddService addService;
    private SubtractService subtractService;
    private MultiplyService multiplyService;
    private DivideService divideService;

    /**
     * Creates a calculator component, taking references to dependent services.
     *
     * @param addService      the service for performing addition
     * @param subtractService the service for performing subtraction
     * @param multiplyService the service for performing multiplication
     * @param divideService   the service for performing division
     */
    public CalculatorServiceImpl(@Reference(name = "addService")AddService addService,
                                 @Reference(name = "subtractService")SubtractService subtractService,
                                 @Reference(name = "multiplyService")MultiplyService multiplyService,
                                 @Reference(name = "divideService")DivideService divideService) {
        this.addService = addService;
        this.subtractService = subtractService;
        this.multiplyService = multiplyService;
        this.divideService = divideService;
    }

    public double add(double n1, double n2) {
        return addService.add(n1, n2);
    }

    public double subtract(double n1, double n2) {
        return subtractService.subtract(n1, n2);
    }

    public double multiply(double n1, double n2) {
        return multiplyService.multiply(n1, n2);
    }

    public double divide(double n1, double n2) {
        return divideService.divide(n1, n2);
    }


}
