package calculator;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Scope;

/**
 * @version $Rev$ $Date$
 */
@Scope("COMPOSITE")
@EagerInit
public class EagerCalculator implements CalculatorService {
    public Double add(Double n1, Double n2) {
        return null;
    }

    public Double subtract(Double n1, Double n2) {
        return null;
    }

    public Double multiply(Double n1, Double n2) {
        return null;
    }

    public Double divide(Double n1, Double n2) {
        return null;
    }

    @Init
    public void init() {
        System.out.println("Initialized calculator");
    }
}
