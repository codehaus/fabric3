package calculator;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
public class TestComponent extends TestCase {
    @Reference
    public CalculatorService calculator;

    public void testCalculator() {
        double result = calculator.add(2.0, 3.0);
        System.out.println("2.0 + 3.0 = " + result);
        assertEquals(5.0, result);
    }
}
