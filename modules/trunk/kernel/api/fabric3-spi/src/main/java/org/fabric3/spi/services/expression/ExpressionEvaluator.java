package org.fabric3.spi.services.expression;

/**
 * Evalautes an expression against some information set. For example, an implementation may return the value of a system property. One application of
 * an ExpressionEvaluators is to provide a mechanism to override attrbiutes and values in a composite.
 *
 * @version $Revision$ $Date$
 */
public interface ExpressionEvaluator {

    /**
     * Evaluate an expression, returning the corresponding value or null.
     *
     * @param expression the expression to evaluate
     * @return the evaluated expression or null
     */
    String evaluate(String expression);

}
