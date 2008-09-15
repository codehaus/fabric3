package org.fabric3.fabric.services.expression;

import org.fabric3.spi.services.expression.ExpressionEvaluator;

/**
 * Returns an value set through a system property.
 *
 * @version $Revision$ $Date$
 */
public class SystemPropertyEvaluator implements ExpressionEvaluator {

    public String evaluate(String expression) {
        return System.getProperty(expression);
    }
}
