package org.fabric3.fabric.services.expression;

import org.fabric3.spi.services.expression.ExpressionEvaluator;

/**
 * Returns an value set through an environment variable.
 *
 * @version $Revision$ $Date$
 */
public class EnvironmentPropertyEvaluator implements ExpressionEvaluator {

    public String evaluate(String expression) {
        return System.getenv(expression);
    }
}