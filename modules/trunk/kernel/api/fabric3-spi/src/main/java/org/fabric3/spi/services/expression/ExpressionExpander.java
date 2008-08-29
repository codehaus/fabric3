package org.fabric3.spi.services.expression;

/**
 * Expands strings containing expressions delimited by '${' and '}' by delegating to an ExpressionEvaluator. Expression values may be sourced using a
 * variety of methods, including system and environment properties. Expression expansion is used to subsitute configuration values specified in a
 * composite file with runtime values. For example:
 * <pre>
 * <binding.ws uri="http://${myservice.endpoint}/>
 * </pre>
 *
 * @version $Revision$ $Date$
 */
public interface ExpressionExpander {

    /**
     * A string value containing an expression or expressions to expand. The string may contain multiple expressions.
     *
     * @param value the value to expand
     * @return the expanded string
     * @throws ExpressionExpansionException if an error occurs evaluating an expression
     */
    String expand(String value) throws ExpressionExpansionException;

}
